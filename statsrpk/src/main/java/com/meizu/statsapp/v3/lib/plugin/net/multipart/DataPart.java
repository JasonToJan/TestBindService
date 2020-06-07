
package com.meizu.statsapp.v3.lib.plugin.net.multipart;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class implements a part of a Multipart post object that consists of byte
 * array.
 */
public class DataPart extends PartBase {

    /** Default content encoding of file attachments. */
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /** Default charset of file attachments. */
    public static final String DEFAULT_CHARSET = "ISO-8859-1";

    /** Default transfer encoding of file attachments. */
    public static final String DEFAULT_TRANSFER_ENCODING = "binary";

    /** Attachment's file name */
    protected static final String FILE_NAME = "; filename=";

    /** Attachment's file name as a byte array */
    private static final byte[] FILE_NAME_BYTES =
            EncodingUtils.getAsciiBytes(FILE_NAME);

    /** Source of the file part. */
    private byte[] source;
    private String filename;

    /**
     * FilePart Constructor.
     * 
     * @param name the name for this part
     * @param partSource the source for this part
     * @param contentType the content type for this part, if <code>null</code>
     *            the {@link #DEFAULT_CONTENT_TYPE default} is used
     * @param charset the charset encoding for this part, if <code>null</code>
     *            the {@link #DEFAULT_CHARSET default} is used
     */
    public DataPart(String name, String filename, byte[] data, String contentType, String charset) {

        super(
                name,
                contentType == null ? DEFAULT_CONTENT_TYPE : contentType,
                charset == null ? "ISO-8859-1" : charset,
                DEFAULT_TRANSFER_ENCODING);

        if (data == null) {
            throw new IllegalArgumentException("Source may not be null");
        }
        this.filename = filename;
        this.source = data;
    }

    /**
     * FilePart Constructor.
     * 
     * @param name the name for this part
     * @param partSource the source for this part
     */
    public DataPart(String name, String filename, byte[] data) {
        this(name, filename, data, null, null);
    }

    public DataPart(String name, byte[] data) {
        this(name, name, data, null, null);
    }

    /**
     * Write the disposition header to the output stream
     * 
     * @param out The output stream
     * @throws IOException If an IO problem occurs
     * @see Part#sendDispositionHeader(OutputStream)
     */
    @Override
    protected void sendDispositionHeader(OutputStream out)
            throws IOException {
        super.sendDispositionHeader(out);
        if (filename != null) {
            out.write(FILE_NAME_BYTES);
            out.write(QUOTE_BYTES);
            out.write(EncodingUtils.getAsciiBytes(filename));
            out.write(QUOTE_BYTES);
        }
    }

    /**
     * Write the data in "source" to the specified stream.
     * 
     * @param out The output stream.
     * @throws IOException if an IO problem occurs.
     * @see Part#sendData(OutputStream)
     */
    @Override
    protected void sendData(OutputStream out) throws IOException {
        if (lengthOfData() == 0) {

            // this file contains no data, so there is nothing to send.
            // we don't want to create a zero length buffer as this will
            // cause an infinite loop when reading.
            return;
        }

        byte[] tmp = new byte[4096];
        ByteArrayInputStream instream = new ByteArrayInputStream(source);
        try {
            int len;
            while ((len = instream.read(tmp)) >= 0) {
                out.write(tmp, 0, len);
            }
        } finally {
            // we're done with the stream, close it
            instream.close();
        }
    }

    /**
     * Return the length of the data.
     * 
     * @return The length.
     * @see Part#lengthOfData()
     */
    @Override
    protected long lengthOfData() {
        return source.length;
    }

}
