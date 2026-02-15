// AndroidIOFactory.java
package com.example.hackathon.persistentdata.io;

import android.content.Context;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class AndroidIOFactory implements IOFactory {
    private final Context app;

    public AndroidIOFactory(Context ctx) { this.app = ctx.getApplicationContext(); }

    private File resolve(String logical) { return new File(app.getFilesDir(), logical + ".csv"); }

    @Override public Reader reader(String logicalName) {
        try {
            File f = resolve(logicalName);
            if (!f.exists()) return null;
            return new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
        } catch (Exception e) { return null; }
    }

    @Override public Writer writer(String logicalName) {
        try {
            File f = resolve(logicalName);
            return new OutputStreamWriter(new FileOutputStream(f, false), StandardCharsets.UTF_8);
        } catch (Exception e) { return null; }
    }

    // Optional helper for bootstrap
    public OutputStream rawOutput(String logicalName, boolean append) throws IOException {
        return new FileOutputStream(resolve(logicalName), append);
    }

    public File fileFor(String logicalName) { return resolve(logicalName); }
}
