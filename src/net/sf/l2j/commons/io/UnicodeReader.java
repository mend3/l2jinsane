package net.sf.l2j.commons.io;

import java.io.*;

public class UnicodeReader extends Reader {
    private static final int BOM_SIZE = 4;
    private final PushbackInputStream internalIn;
    private final String defaultEnc;
    private InputStreamReader internalIn2 = null;

    public UnicodeReader(InputStream in, String defaultEnc) {
        this.internalIn = new PushbackInputStream(in, 4);
        this.defaultEnc = defaultEnc;
    }

    public String getDefaultEncoding() {
        return this.defaultEnc;
    }

    public String getEncoding() {
        if (this.internalIn2 == null)
            return null;
        return this.internalIn2.getEncoding();
    }

    protected void init() throws IOException {
        String encoding;
        int unread;
        if (this.internalIn2 != null)
            return;
        byte[] bom = new byte[4];
        int n = this.internalIn.read(bom, 0, bom.length);
        if (bom[0] == -17 && bom[1] == -69 && bom[2] == -65) {
            encoding = "UTF-8";
            unread = n - 3;
        } else if (bom[0] == -2 && bom[1] == -1) {
            encoding = "UTF-16BE";
            unread = n - 2;
        } else if (bom[0] == -1 && bom[1] == -2) {
            encoding = "UTF-16LE";
            unread = n - 2;
        } else if (bom[0] == 0 && bom[1] == 0 && bom[2] == -2 && bom[3] == -1) {
            encoding = "UTF-32BE";
            unread = n - 4;
        } else if (bom[0] == -1 && bom[1] == -2 && bom[2] == 0 && bom[3] == 0) {
            encoding = "UTF-32LE";
            unread = n - 4;
        } else {
            encoding = this.defaultEnc;
            unread = n;
        }
        if (unread > 0)
            this.internalIn.unread(bom, n - unread, unread);
        this.internalIn2 = (encoding == null) ? new InputStreamReader(this.internalIn) : new InputStreamReader(this.internalIn, encoding);
    }

    public void close() throws IOException {
        init();
        this.internalIn.close();
        this.internalIn2.close();
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        init();
        return this.internalIn2.read(cbuf, off, len);
    }
}
