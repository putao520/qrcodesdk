package common.qrcode;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import common.java.file.FileHelper;
import common.java.nlogger.nlogger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Hashtable;

public class QrCode {
    private static final String CHARSET = "UTF-8";
    private static final String FORMAT_NAME = "JPG";
    // 二维码尺寸
    private static final int QRCODE_SIZE = 200;
    // LOGO宽度
    private static final int WIDTH = 60;
    // LOGO高度
    private static final int HEIGHT = 60;

    /**
     * user: Rex
     * date: 2016年12月29日  上午12:31:29
     * @param content 二维码内容
     * @param logoImgPath Logo
     * @param needCompress 是否压缩Logo
     * @return 返回二维码图片
     * @throws WriterException
     * @throws IOException
     * BufferedImage
     * TODO 创建二维码图片
     */
    private static BufferedImage createImage(String content, String logoImgPath, boolean needCompress){
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1);
        BufferedImage image = null;
        try{
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            if (logoImgPath == null || "".equals(logoImgPath)) {
                return image;
            }
            // 插入图片
            QrCode.insertImage(image, logoImgPath, needCompress);
        }
        catch (Exception e){
            nlogger.debugInfo("生成二维码失败");
        }
        return image;
    }

    /**
     * user: Rex
     * date: 2016年12月29日  上午12:30:09
     * @param source 二维码图片
     * @param logoImgPath Logo
     * @param needCompress 是否压缩Logo
     * @throws IOException
     * void
     * TODO 添加Logo
     */
    private static void insertImage(BufferedImage source, String logoImgPath, boolean needCompress){
        File file = new File(logoImgPath);
        if (!file.exists()) {
            return;
        }
        try{
            Image src = ImageIO.read(new File(logoImgPath));
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            if (needCompress) { // 压缩LOGO
                if (width > WIDTH) {
                    width = WIDTH;
                }

                if (height > HEIGHT) {
                    height = HEIGHT;
                }

                Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics g = tag.getGraphics();
                g.drawImage(image, 0, 0, null); // 绘制缩小后的图
                g.dispose();
                src = image;
            }

            // 插入LOGO
            Graphics2D graph = source.createGraphics();
            int x = (QRCODE_SIZE - width) / 2;
            int y = (QRCODE_SIZE - height) / 2;
            graph.drawImage(src, x, y, width, height, null);
            Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
            graph.setStroke(new BasicStroke(3f));
            graph.draw(shape);
            graph.dispose();
        }
        catch (Exception e){
            nlogger.debugInfo("插入logo失败");
        }
    }

    /**
     * user: Rex
     * date: 2016年12月29日  上午12:32:32
     * @param content 二维码内容
     * @param logoImgPath Logo
     * @param destPath 二维码输出路径
     * @param needCompress 是否压缩Logo
     * @throws Exception
     * void
     * TODO 生成带Logo的二维码
     */
    public static void encode(String content, String logoImgPath, String destPath, boolean needCompress) throws Exception {
        BufferedImage image = QrCode.createImage(content, logoImgPath, needCompress);
        FileHelper.createFile(destPath);
        ImageIO.write(image, FORMAT_NAME, new File(destPath));
    }

    /**
     * user: Rex
     * date: 2016年12月29日  上午12:35:44
     * @param content 二维码内容
     * @param destPath 二维码输出路径
     * @throws Exception
     * void
     * TODO 生成不带Logo的二维码
     */
    public static void encode(String content, String destPath) throws Exception {
        QrCode.encode(content, null, destPath, false);
    }

    /**
     * user: Rex
     * date: 2016年12月29日  上午12:36:58
     * @param content 二维码内容
     * @param logoImgPath Logo
     * @param output 输出流
     * @param needCompress 是否压缩Logo
     * @throws Exception
     * void
     * TODO 生成带Logo的二维码，并输出到指定的输出流
     */
    public static void encode(String content, String logoImgPath, OutputStream output, boolean needCompress){
        BufferedImage image = QrCode.createImage(content, logoImgPath, needCompress);
        try{
            ImageIO.write(image, FORMAT_NAME, output);
        }
        catch (Exception e){
            nlogger.debugInfo("写入二维码文件失败");
        }
    }

    /**
     * user: Rex
     * date: 2016年12月29日  上午12:38:02
     * @param content 二维码内容
     * @param output 输出流
     * @throws Exception
     * void
     * TODO 生成不带Logo的二维码，并输出到指定的输出流
     */
    public static void encode(String content, OutputStream output){
        QrCode.encode(content, null, output, false);
    }

    /**
     * user: Rex
     * date: 2016年12月29日  上午12:39:10
     * @param input 二维码
     * @return 返回解析得到的二维码内容
     * @throws Exception
     * String
     * TODO 二维码解析
     */
    public static String decode(InputStream input){
        String resultStr = null;
        BufferedImage image;
        try{
            image = ImageIO.read(input);
            if (image == null) {
                return null;
            }
            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result;
            Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
            result = new MultiFormatReader().decode(bitmap, hints);
            resultStr = result.getText();
        }
        catch (Exception e){
            nlogger.debugInfo(e, "qrCode decode Error");
        }
        return resultStr;
    }

    /**
     * user: Rex
     * date: 2016年12月29日  上午12:39:48
     * @param path 二维码存储位置
     * @return 返回解析得到的二维码内容
     * @throws Exception
     * String
     * TODO 二维码解析
     */
    public static String decode(String path){
        try( FileInputStream img = new FileInputStream(new File(path)) ){
            return QrCode.decode(img);
        }
        catch (Exception e){
            nlogger.debugInfo(e,"二维码分析失败");
        }
        return null;
    }

    public static final byte[] createQR(String content){
        return QrCode.createQR(content, null);
    }
    public static final byte[] createQR(String content, String logoPath){
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()){
            BufferedImage img = QrCode.createImage(content, logoPath, false);
            ImageIO.write(img, FORMAT_NAME,out);
            return out.toByteArray();
        }
        catch (Exception e){
            nlogger.debugInfo(e, "二维码生成失败!");
        }
        return null;
    }


    /*
    // 使用例子
    public static void main(String[] args) throws FileNotFoundException,Exception{
        String dir = "D:/1.jpg";
        String content = "https://blog.csdn.net/zhang18330699274";
        String logoImgPath = "H:/Users/Administrator/Desktop/logo.png";
        File file = new File(dir);
        QrCode.encode(content, logoImgPath, new FileOutputStream(file), true);
    }
     */
}
