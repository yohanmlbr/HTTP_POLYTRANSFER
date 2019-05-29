
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    private BufferedWriter bw;
    private BufferedReader br;
    private Socket sClient;
    private static String folder="D:\\Desktop\\Client\\";

    public Client(String nomServ, int port){
        try{
            sClient = new Socket(InetAddress.getByName(nomServ), port);
            System.out.println("Connexion réussie sur le serveur : " + nomServ);
            bw = new BufferedWriter(new OutputStreamWriter(sClient.getOutputStream(), StandardCharsets.UTF_8));
            br = new BufferedReader(new InputStreamReader(sClient.getInputStream()));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public int getFile(String nomFich, String nomHost){

        try {
            bw.write("GET " + nomFich + " HTTP/1.1\r\n");
            bw.write("Connection: keep-alive\r\n");
            bw.write("Host: " + nomHost + "\r\n\r\n");
            bw.flush();
            readResponse(nomFich);
            return 1;
        } catch (Exception e){
            System.out.println(e.getMessage());
            return -1;
        }

    }

    public int putFile(String path, String nomFich, String nomHost){

        try{
            BufferedReader fReader = new BufferedReader(new FileReader(path + nomFich));
            System.out.println(path + nomFich);
            String line;
            String type;
            ArrayList<String> content = new ArrayList<>();
            while ((line = fReader.readLine()) != null)
            {
                content.add(line);
            }
            System.out.println(content.size());
            fReader.close();

            bw.write("PUT /" + nomFich + " HTTP/1.1\r\n");
            bw.write("Connection: keep-alive\r\n");
            bw.write("Host: " + nomHost + "\r\n");
            bw.write("\r\n");
            for(String s : content){
                System.out.println(s);
                bw.write(s+"\r\n");
            }
            bw.flush();

        } catch (IOException ie) {
            System.out.println("File not found");
            return -3;
        }
        return 1;

    }

    public int readResponse(String nomFich){
        try {
            String type = "";
            ArrayList<String> reponse = new ArrayList<>();
            ArrayList<String> content = new ArrayList<>();
            String line="";
            boolean startContent = false;

            while((line=br.readLine())!= null){
                System.out.println(line);
                reponse.add(line);
                if(line.contains("Content-Type")) {
                    String pattern = "Content-Type: (\\w+\\/\\w+)";
                    Pattern p = Pattern.compile(pattern);
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        type = m.group(1);
                    }
                }

                if(startContent)
                    content.add(line+"\n");

                if(line.equals(""))
                    startContent = true;

                if(!br.ready())
                    break;
            }

            switch(type){
                case "text/plain" :{
                    writeFile(nomFich,content);
                    break;
                }
                case "text/html" :{
                    writeFile(nomFich,content);
                    break;
                }
                case "image/jpeg" :{
                    writeFile(nomFich,content);
                    break;
                }
                case "image/png" :{
                    writeFile(nomFich,content);
                    break;
                }
            }

            sClient.close();
            return 2;
        } catch (Exception e){
            System.out.println(e.getMessage());
            return -2;
        }
    }

    // .+\.(.+)

    public int writeFile(String nomFich, ArrayList<String> contenu){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (String s: contenu) {
            try{
                baos.write(s.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        byte[] bytes = baos.toByteArray();
        try (FileOutputStream fos = new FileOutputStream("D:\\Desktop\\Client\\" + nomFich)) {
            fos.write(bytes);
            System.out.println(bytes.length);
            //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        } catch (IOException iex){
            System.out.println(iex.getMessage());
        }

        return 0;
    }

    public static void main(String[] args){
        String fileget="test_get_serveur_client.txt";
        String fileput="test_put_client_serveur.txt";

        Client clientHTTP = new Client("localhost", 8080);

        //clientHTTP.putFile(folder,fileput,"localhost");
        clientHTTP.getFile(fileget, "TEST");
    }

}
