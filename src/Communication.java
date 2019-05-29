import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Communication implements Runnable {
    private Socket sServeur;
    private BufferedReader br;
    private BufferedWriter bw;

    public Communication(Socket s){
        this.sServeur=s;
        System.out.println("new client");
    }

    public void response(BufferedReader br){
        try{
            if(br.ready()) {
                String filename = "";
                ArrayList<ArrayList<String>> tab = readBuffer(br);
                ArrayList<String> header = tab.get(0);
                ArrayList<String> contenu = tab.get(1);
                String line = header.get(0);
                if (line.charAt(0) == 'G' && line.charAt(1) == 'E' && line.charAt(2) == 'T') {
                    int i = 4;
                    while (line.charAt(i) != ' ') {
                        filename = filename.concat(Character.toString(line.charAt(i)));
                        i++;
                    }
                    if (checkFileExistence(filename) == 0) {
                        sendResponseOK(filename);
                    }
                    else{
                        sendResponseERROR();
                    }
                }
                if (line.charAt(0) == 'P' && line.charAt(1) == 'U' && line.charAt(2) == 'T') {
                    int i = 4;
                    while (line.charAt(i) != ' ') {
                        filename = filename.concat(Character.toString(line.charAt(i)));
                        i++;
                    }
                    writeFile(filename,contenu);
                    //TODO : ecrire sur un fichier wola

                }
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void sendResponseERROR(){
        try {
            bw.write("HTTP/1.1 404 File Not Found\r\n");
            bw.write("Connection: close");
            bw.write("\r\n");
            bw.flush();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }


    public void sendResponseOK(String filename){
        try{
            String[] ct=contentType(filename);
            bw.write("HTTP/1.1 200 OK\r\n");
            bw.write("Content-Type: "+ct[0]+ct[1]+"\r\n");
            bw.write("\r\n");

            //lecture fichier texte
            if(ct[0].equals("image") || ct[0].equals("text")) {
                InputStream flux = new FileInputStream(Server.getFolder() + filename);
                InputStreamReader lecture = new InputStreamReader(flux);
                BufferedReader buff = new BufferedReader(lecture);
                String textline;
                while ((textline = buff.readLine()) != null) {
                    bw.write(textline + "\r\n");
                    System.out.println(textline);
                }
                buff.close();
            }

            /*else if(ct[0].equals("")){
                File img = new File(Server.getFolder()+filename);
                BufferedImage bufferedImage = ImageIO.read(img);
                WritableRaster raster = bufferedImage .getRaster();
                DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();
                System.out.println(data.getData().length);
                for(int i=0;i<data.getData().length;i++){
                    bw.write(data.getData()[i]);
                    System.out.println(data.getData()[i]);
                }
            }*/

            bw.flush();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public int checkFileExistence(String filename){
        File f=new File(Server.getFolder() + filename);
        if(!f.isFile()) {
            System.out.println("ERROR : File doesn't exist");
            return 404;
        }
        else
            return 0;
    }

    public String[] contentType(String filename){
        String[] ct=new String[2];
        String ext="";
        boolean point=false;
        for(int i=0;i<filename.length();i++){
            if(point){
                ext=ext.concat(Character.toString(filename.charAt(i)));
            }
            if(filename.charAt(i)=='.'){
                point=true;
            }
        }
        switch (ext) {
            case "txt":
                ct[0] = "text";
                ct[1] = "/plain";
                break;
            case "html":
                ct[0] = "text";
                ct[1] = "/html";
                break;
            case "jpg":
                ct[0] = "image";
                ct[1] = "/jpeg";
                break;
            case "png":
                ct[0] = "image";
                ct[1] = "/png";
                break;
            default:
                ct[0] = "dont";
                ct[1] = "/know";
                break;
        }
        return ct;
    }

    public ArrayList<ArrayList<String>> readBuffer(BufferedReader br){
        ArrayList<ArrayList<String>> tab=new ArrayList<>();
        ArrayList<String> header = new ArrayList<>();
        ArrayList<String> contenu = new ArrayList<>();
        try{
            System.out.println("----------------");
            String line=br.readLine();
            while (!line.equals("")){
                header.add(line);
                System.out.println(line);
                line=br.readLine();
            }
            String request = header.get(0);
            if (request.charAt(0) == 'P' && request.charAt(1) == 'U' && request.charAt(2) == 'T') {
                System.out.println("---CRLF---");
                String line2 = br.readLine();
                while (line2 != null) {
                    contenu.add(line2 + "\r\n");
                    System.out.println(line2);
                    line2 = br.readLine();
                    if (!br.ready())
                        break;
                }
            }
            System.out.println("----------------");
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
        tab.add(header);
        tab.add(contenu);
        return tab;
    }

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
        try (FileOutputStream fos = new FileOutputStream("D:\\Desktop\\Web" + nomFich)) {
            fos.write(bytes);
            //System.out.println(bytes.length);
            //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        } catch (IOException iex){
            System.out.println(iex.getMessage());
        }

        return 0;
    }




    @Override
    public void run() {
        try {
            while(true) {
                br = new BufferedReader(new InputStreamReader(sServeur.getInputStream(), StandardCharsets.ISO_8859_1));
                bw = new BufferedWriter(new OutputStreamWriter(sServeur.getOutputStream()));
                response(br);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
