package main.pt.ipleiria.estg.dei.utils;


import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public abstract class Utils{
    private static Logger logger = new Logger<>(Utils.class);

    public static byte[] convertToByte(Object object) {
        try ( ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            out.close();
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error( "Couldn't convert to byte: " + e.getMessage());
            throw new IllegalArgumentException("Couldn't convert to byte: " + e.getMessage());
        }
    }

    public static Object fromByte(byte[] yourBytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);) {
            ObjectInput in = new ObjectInputStream(bis);
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Couldn't convert to byte: " + e.getMessage());
            throw new IllegalArgumentException("Couldn't convert to byte: " + e.getMessage());
        }
    }

    public static void writeCsv(List<List<String>>  rs, String pathname) {
        StringBuilder sb = new StringBuilder();
        try (PrintWriter writer = new PrintWriter(new File(pathname + ".csv"))) {
            rs.forEach(result->{
                sb.append( String.join(", ", result)).append("\n");
            });
            writer.write(sb.toString());
        }catch (FileNotFoundException e) {
            logger.warn(e.getMessage());
        }
    }

    public static String parse(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        return dateFormat.format(date);
    }

    public static String parseToDay(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }

    public static String atEndOfDay(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return parse(localDateTimeToDate(endOfDay));
    }
    private static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
