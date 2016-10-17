package org.servalproject.api;

import android.os.Bundle;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Command {
    private Map<String, byte[]> extras;
    private String action;

    public Command(String action) {
        extras = new HashMap<String, byte[]>();
        this.action = action;
    }

    public boolean contains(String key) {
        return this.extras.containsKey(key);
    }

    /////////////
    // Setters //
    /////////////
    public void putExtra(String key, byte[] value) {
        extras.put(key, value);
    }

    public void putExtra(String key, String value) {
        extras.put(key, value.getBytes());
    }

    public void putExtra(String key, int value) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(value);

        extras.put(key, b.array());
    }

    public void putExtra(String key, long value) {
        ByteBuffer b = ByteBuffer.allocate(8);
        b.putLong(value);

        extras.put(key, b.array());
    }

    /////////////
    // Getters //
    /////////////
    public String getAction() {
        return action;
    }

    public byte[] getExtra(String key) {
        return extras.get(key);
    }

    public String getExtraString(String key) {
        return new String(extras.get(key));
    }

    public int getExtraInt(String key) {
        ByteBuffer buffer = ByteBuffer.wrap(extras.get(key));

        return buffer.getInt();
    }

    public long getExtraLong(String key) {
        ByteBuffer buffer = ByteBuffer.wrap(extras.get(key));

        return buffer.getLong();
    }

    /////////////////////
    // Format Shifting //
    /////////////////////
    public byte[] asBytes() {
        int numBytes = 0;
        byte[] act = action.getBytes();

        numBytes += 4 + act.length; // size field plus length

        numBytes += 4; // Extras count

        int numExtras = 0;
        for (Map.Entry<String, byte[]> entry : extras.entrySet()) {
            numBytes += 4 + entry.getKey().getBytes().length;
            numBytes += 4 + entry.getValue().length;

            numExtras++;
        }

        ByteBuffer buffer = ByteBuffer.allocate(numBytes);
        buffer.putInt(act.length);
        buffer.put(act);

        buffer.putInt(numExtras);

        for (Map.Entry<String, byte[]> entry : extras.entrySet()) {
            byte[] key = entry.getKey().getBytes();
            byte[] value = entry.getValue();

            buffer.putInt(key.length);
            buffer.put(key);
            buffer.putInt(value.length);
            buffer.put(value);
        }

        return buffer.array();
    }

    public static Command parse(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int actLen = buffer.getInt();

        byte[] act = new byte[actLen];
        buffer.get(act, 0, actLen);
        String action = new String(act);

        Command cmd = new Command(action);

        int extraCount = buffer.getInt();
        for (int i = 0; i < extraCount; i++) {
            int keyLen = buffer.getInt();
            byte[] keyArr = new byte[keyLen];
            buffer.get(keyArr, 0, keyLen);
            String key = new String(keyArr);

            int valLen = buffer.getInt();
            byte[] valArr = new byte[valLen];
            buffer.get(valArr, 0, valLen);

            cmd.putExtra(key, valArr);
        }

        return cmd;
    }

    public Bundle asBundle() {
        Bundle b = new Bundle();

        for (Map.Entry<String, byte[]> extra : extras.entrySet()) {
            b.putByteArray(extra.getKey(), extra.getValue());
        }

        return b;
    }
}
