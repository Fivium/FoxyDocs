/*
Copyright (c) 2013, Fivium Ltd.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of Fivium Ltd nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package net.foxopen.foxydocs.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import net.foxopen.foxydocs.FoxyDocs;

public class ResourceManager {
  public static void duplicateResource(String resource, String path) throws IOException {
    duplicateResource(resource, new File(path));
  }

  public static void duplicateResource(String resource, File target) throws IOException {
    copyFile(getInternalFile(resource), target);
  }

  /**
   * Get an internal file (in the File System or inside the JAR)
   * 
   * @param uri
   *          The file path
   * @return The file as InputStream
   * @throws FileNotFoundException
   */
  public static InputStream getInternalFile(String uri) throws FileNotFoundException {
    InputStream resource = FoxyDocs.class.getClassLoader().getResourceAsStream(uri);
    if (resource == null)
      throw new FileNotFoundException("Resource not found : " + uri);
    return resource;
  }

  public static void copyFile(InputStream sourceInputStream, File destFile) throws IOException {
    FileChannel destination = null;
    ReadableByteChannel source = null;
    final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

    try {
      source = Channels.newChannel(sourceInputStream);
      destination = new FileOutputStream(destFile).getChannel();

      // Copy the file from the source to the target file
      while (source.read(buffer) != -1) {
        buffer.flip();
        destination.write(buffer);
        buffer.compact();
      }
      buffer.flip();
      while (buffer.hasRemaining()) {
        destination.write(buffer);
      }

    }
    // Is something goes wrong, those the channels anyway
    finally {
      if (source != null) {
        source.close();
      }
      if (destination != null) {
        destination.close();
      }
    }
  }
}
