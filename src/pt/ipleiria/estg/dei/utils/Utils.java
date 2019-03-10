package pt.ipleiria.estg.dei.utils;

import org.sleuthkit.autopsy.coreutils.Logger;

import java.io.*;
import java.util.logging.Level;

public abstract class Utils{
    private static Logger logger = Logger.getLogger(Utils.class.getName());

    public static byte[] convertToByte(Object object) {
        try ( ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            out.close();
            return bos.toByteArray();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Couldn't convert to byte: " + e.getMessage());
            throw new IllegalArgumentException("Couldn't convert to byte: " + e.getMessage());
        }
    }

    public static Object fromByte(byte[] yourBytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);) {
            ObjectInput in = new ObjectInputStream(bis);
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Couldn't convert to byte: " + e.getMessage());
            throw new IllegalArgumentException("Couldn't convert to byte: " + e.getMessage());
        }
    }
}
