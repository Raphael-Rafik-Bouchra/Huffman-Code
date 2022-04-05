import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        int argslen = args.length;

        if (argslen < 2) {
            System.out.println("Invalid arguments");
            System.exit(1);
        } else if (argslen == 2){
            //Decompression
            String option = args[0];
            String inputFile = args[1];

            if(option.equals("d")){
                decompress(inputFile);
            }
            else {
                System.out.println("Invalid arguments");
                System.exit(1);
            }

        }
        else if (argslen == 3){
            //Compression
            String option = args[0];
            String inputFile = args[1];
            String n = args[2];

            if(option.equals("c")){
                compress(inputFile,Integer.parseInt(n));
            }
            else {
                System.out.println("Invalid arguments");
                System.exit(1);
            }
        }
        else {
            System.out.println("Invalid arguments");
            System.exit(1);
        }
    }

    private static void compress (String inputFilePath, int n) {
        File file = new File(inputFilePath);
        BufferedInputStream bis = null;
        FileInputStream fis = null;

        TreeMap<String, Integer> freqMap = new TreeMap<>();

        long startTime = System.nanoTime();
        try {
            fis = new FileInputStream(file);

            bis = new BufferedInputStream(fis);

            byte[] buffer = new byte[n];

            int bytes = 0;
            String fileContent;

            while ((bytes = bis.read(buffer)) != -1) {
                fileContent = new String(buffer, 0, bytes);
                //System.out.print(fileContent);

                if (freqMap.containsKey(fileContent)) {
                    int v = freqMap.get(fileContent);
                    freqMap.put(fileContent, v + 1);
                } else {
                    freqMap.put(fileContent, 1);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: File Not Found");
            e.printStackTrace();
        }catch (IOException e) {
            System.out.println("ERROR: File Not Found");
            e.printStackTrace();
        }finally
        {
            try{
                if(bis != null && fis!=null)
                {
                    fis.close();
                    bis.close();
                }
            }catch(IOException ioe)
            {
                System.out.println("ERROR in InputStream close(): " + ioe);
            }
        }

        HuffmanTree huffmanTree = new HuffmanTree(freqMap);
        huffmanTree.build();
        huffmanTree.code();


        try {
            int endindex = inputFilePath.length() - file.getName().length();
            String outpath = inputFilePath.substring(0,endindex);
            String compFile = outpath +  "6217." + Integer.toString(n) + "." + file.getName() + ".hc";
            try (

                    Writer writer = new OutputStreamWriter(new FileOutputStream(compFile, false), StandardCharsets.ISO_8859_1)
            ) {
                freqMap.entrySet().forEach((cur) -> {
                    try {

                        int i;
                        for(i=0;i<cur.getKey().length()-1;i++){
                            writer.write( (int) cur.getKey().charAt(i) + "-");
                        }
                        writer.write( (int) cur.getKey().charAt(i) + " ");
                        //writer.write(  stringToHex(cur.getKey()) + " ");          --> For Maven Project
                        writer.write((int) cur.getValue() + " ");
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                writer.write("\n");




                try {
                    FileWriter tempWriter = new FileWriter("tempFile.txt");
                    long tempFileSize = 0L;


                    FileInputStream fis2 = new FileInputStream(file);

                    BufferedInputStream bis2 = new BufferedInputStream(fis2);

                    byte[] buffer2 = new byte[n];

                    int bytes2 = 0;
                    String fileContent2;


                    while ((bytes2 = bis2.read(buffer2)) != -1) {
                        fileContent2 = new String(buffer2, 0, bytes2);
                        String out = huffmanTree.getCode(fileContent2);

                        tempFileSize += out.length();
                        tempWriter.write(out);

                    }


                    int zeroPaddingNum = 0;

                    if ((tempFileSize % 7) != 0) {
                        zeroPaddingNum = 7 - ((int)(tempFileSize % 7));
                        for (int i = 0; i < zeroPaddingNum; i++) {
                            tempWriter.write("0");
                        }
                    }
                    tempWriter.close();


                    writer.write(String.valueOf(zeroPaddingNum));
                    writer.write("\n");
                        File tempFile = new File("tempFile.txt");
                        FileInputStream fis3 = new FileInputStream(tempFile);
                        BufferedInputStream bis3 = new BufferedInputStream(fis3);
                        byte[] buffer3 = new byte[7];
                        int bytes3 = 0;
                        String fileContent3;

                        while ((bytes3 = bis3.read(buffer3)) != -1) {
                            fileContent3 = new String(buffer3, 0, bytes3);

                            for(int j=0;j<fileContent3.length()/7;j++){
                                Integer binary = Integer.parseInt(fileContent3.substring(7*j,7*(j+1)), 2);
                                writer.write(binary.byteValue());
                            }


                        }

                    tempFile.delete();

                } catch (FileNotFoundException e) {
                    System.out.println("ERROR: File Not Found");
                    e.printStackTrace();
                }catch (IOException e) {
                    System.out.println("ERROR: File Not Found");
                    e.printStackTrace();
                }finally
                {
                    try{
                        if(bis != null && fis!=null)
                        {
                            fis.close();
                            bis.close();
                        }
                    }catch(IOException ioe)
                    {
                        System.out.println("ERROR in InputStream close(): " + ioe);
                    }
                }

            }

            double compTime = System.nanoTime() - startTime;
            huffmanTree.displayCode();

            double compRatio = (double)Files.size(Paths.get(compFile))/(double)Files.size(Paths.get(inputFilePath));
            System.out.println(String.format("Compression ratio= %.3f %%",compRatio*100));
            System.out.println("Compression time= " + compTime/ 1000000000 + " Sec");
        } catch (IOException e) {
            System.out.println("File Not Found !!");
        }

    }

    private static void decompress (String inputFile){
        Scanner in;
        File file = new File(inputFile);
        TreeMap<String, Integer> freqMap = new TreeMap<>();
        String zeroPadding = "";
        int zeropaddingNum = 0;
        String charcode = "";
        long startTime = System.nanoTime();
        try {
            in = new Scanner(file);
            if (in.hasNextLine()) {
                String s = in.nextLine();
                String[] values = s.split(" ", 0);
                StringBuilder str = new StringBuilder();
                for (int i = 0; i < values.length; i += 2) {

                    String[] multipleChar = values[i].split("-", 0);
                    for(int j=0;j<multipleChar.length;j++){
                        int c = Integer.parseInt(multipleChar[j]);
                        str.append((char) c);
                    }

                     //str.append(hexToString(values[i]));      --> For Maven Project

                    freqMap.put(str.toString(), Integer.parseInt(values[i + 1]));
                    str.delete(0, str.length());
                }
                HuffmanTree huff = new HuffmanTree(freqMap);
                huff.build();

                if (in.hasNextLine()) {
                    zeroPadding = in.nextLine();
                    zeropaddingNum = Integer.parseInt(zeroPadding);
                }

                File tempFile = new File("tempFile.txt");
                FileWriter tempWriter = new FileWriter("tempFile.txt");
                StringBuilder binaryCode = new StringBuilder();

                charcode = in.useDelimiter("\n").next();
                binaryCode.append(AsciiToBinary(charcode));
                if(in.hasNextLine() == false){
                    binaryCode.delete(binaryCode.length()-zeropaddingNum, binaryCode.length());
                }
                tempWriter.write(binaryCode.toString());
                binaryCode.delete(0, binaryCode.length());
                while(in.hasNext()){
                    charcode = in.useDelimiter("\n").next();

                    binaryCode.append("0001010" + AsciiToBinary(charcode));
                    if(in.hasNextLine() == false){
                        binaryCode.delete(binaryCode.length()-zeropaddingNum, binaryCode.length());
                    }

                    tempWriter.write(binaryCode.toString());
                    binaryCode.delete(0, binaryCode.length());
                }
                tempWriter.close();


                FileInputStream fis2 = new FileInputStream(tempFile);

                BufferedInputStream bis2 = new BufferedInputStream(fis2);

                byte[] buffer2 = new byte[7];

                int bytes2 = 0;
                String fileContent2;
                StringBuilder extracted = new StringBuilder();

                int endindex = inputFile.length() - file.getName().length();
                String outpath = inputFile.substring(0,endindex);
                String decompFile = outpath +  "extracted." + file.getName().substring(0,file.getName().length()-3);
                Writer decompFileWriter = new OutputStreamWriter(new FileOutputStream(decompFile, false), StandardCharsets.ISO_8859_1);

                while ((bytes2 = bis2.read(buffer2)) != -1) {
                    fileContent2 = new String(buffer2, 0, bytes2);
                    extracted.append(fileContent2);
                    if(decodeable(extracted.toString(), huff)){
                        decompFileWriter.write(huff.decode(extracted.toString()));
                        extracted.delete(0,extracted.length());
                    }
                }
                decompFileWriter.close();
                tempFile.delete();

                double decompTime = System.nanoTime() - startTime;
                System.out.println("Decompression time= " + decompTime/ 1000000000 + " Sec");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found !!");
        }catch (IOException e) {
            System.out.println("File Not Found !!");
        }

    }

    public static String AsciiToBinary(String asciiString) {

        byte[] bytes = asciiString.getBytes(StandardCharsets.ISO_8859_1);
        StringBuilder binary = new StringBuilder(bytes.length *7);

        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i];
            for (int j = 0; j < 8; j++) {
                if ((value & 128) == 0) {
                    binary.append(0);
                } else {
                    binary.append(1);
                }
                value <<= 1;
            }

                binary.delete(i * 7, (i * 7) + 1);
        }

        return binary.toString();
    }

    public static boolean decodeable(String code,HuffmanTree huff) {

        boolean flag = true;
        try{
            String decoded = huff.decode(code);
        }catch (StringIndexOutOfBoundsException e){
            flag = false;
        }
        return flag;
    }

    public static String stringToHex(String str) {

        /*
        char[] chars = Hex.encodeHex(str.getBytes(StandardCharsets.ISO_8859_1));
        return String.valueOf(chars);

         */
        return  null;
    }

    public static String hexToString(String hex) {

        String result = "";
        /*
        try {
            byte[] bytes = Hex.decodeHex(hex);
            result = new String(bytes, StandardCharsets.ISO_8859_1);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("Invalid Hex format!");
        }
        */

        return result;
    }

}
