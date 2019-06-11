package main.pt.ipleiria.estg.dei.labcif.bhh.utils;


import main.pt.ipleiria.estg.dei.labcif.bhh.panels.mainPanel.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public abstract class Utils{
    private static LoggerBHH loggerBHH = new LoggerBHH<>(Utils.class);
    private static final SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");

    public static byte[] convertToByte(Object object) {
        try ( ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            out.close();
            return bos.toByteArray();
        } catch (IOException e) {
            loggerBHH.error( "Couldn't convert to byte: " + e.getMessage());
            throw new IllegalArgumentException("Couldn't convert to byte: " + e.getMessage());
        }
    }

    public static Object fromByte(byte[] yourBytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);) {
            ObjectInput in = new ObjectInputStream(bis);
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            loggerBHH.error("Couldn't convert to byte: " + e.getMessage());
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
            loggerBHH.warn(e.getMessage());
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

    public static void copyFile(String source, String dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    public static String createDirectoryIfNotExists(String location) {
        File dir = new File(location);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return location;
    }

    public static void downloadExtractTemplate(Component component) {
        JFileChooser chooser = new JFileChooser();
        int returnValue = chooser.showSaveDialog(component);
        if (returnValue ==  JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String example = "google.com\nfacebook.com";
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.write(example);
                JOptionPane.showMessageDialog(component, "Download with success");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(component, "Error downloading: " + e.getMessage());
            }
        }
    }

    public static String importCSVFile(Component component) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(component);

        if(returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return "";
    }

    public static String getTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return dataFormat.format(timestamp);
    }
}
