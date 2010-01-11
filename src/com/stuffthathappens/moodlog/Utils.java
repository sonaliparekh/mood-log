package com.stuffthathappens.moodlog;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;

public class Utils {
    public static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String trimmed = s.trim();
        return (trimmed.length() == 0) ? null : trimmed;
    }

    public static String loadResToString(int resId, Context ctx, String encoding) {
        Resources res = ctx.getResources();

        InputStream in = res.openRawResource(resId);
        try {
            byte[] buf = new byte[512];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int numRead;
            while ((numRead = in.read(buf)) > 0) {
                bos.write(buf, 0, numRead);
            }
            close(bos);

            return bos.toString(encoding);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            close(in);
        }
    }
    
    /**
     * Moves a file, replacing dest if it exists.
     */
    public static void rename(File src, File dest) throws StorageException {
        if (!src.exists()) {
            return; // nothing to do
        }
        if (dest.exists()) {
            dest.delete();
        }
        dest.getParentFile().mkdirs();
        if (!src.renameTo(dest)) {
            throw new StorageException("Unable to move " + src.getAbsolutePath() + 
                    " to " + dest.getAbsolutePath());            
        }
    }

    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }

}
