package zad1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Futil {

    public static void processDir(String dirName, String resultFileName) {
        Charset inputCharset  = Charset.forName("Cp1250");    // charset of files to be read
        Charset outputCharset = StandardCharsets.UTF_8;       // charset of files to be written

        try (FileChannel outputFileChannel = FileChannel.open(Paths.get(resultFileName),
                                                              StandardOpenOption.WRITE,                  //option to write to the file
                                                              StandardOpenOption.CREATE,                 //option to create a new file if it does not exist
                                                              StandardOpenOption.TRUNCATE_EXISTING)) {   //option to truncate the file if it already exists.
            Files.walkFileTree(Paths.get(dirName), new SimpleFileVisitor<Path>() {
                @Override
                // reading file
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try (FileChannel inputFileChannel = FileChannel.open(file, StandardOpenOption.READ)) { // opening file
                        ByteBuffer buffer = ByteBuffer.allocate((int) inputFileChannel.size());            // allocating buffer
                        inputFileChannel.read(buffer);                                                     // reading file
                        buffer.flip();                                                                     // preparing buffer to be read

                        CharBuffer charBuffer = inputCharset.decode(buffer);                               // decoding file
                        buffer = outputCharset.encode(charBuffer);                                         // encoding file
                        outputFileChannel.write(buffer);                                                   // writing to the file
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}