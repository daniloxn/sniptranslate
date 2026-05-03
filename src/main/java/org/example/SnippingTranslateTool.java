package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import net.sourceforge.lept4j.ILeptonica.leptSetStderrHandler_handler_callback;

import com.deepl.api.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;


public class SnippingTranslateTool {
    static Point startPoint;
    static Point endPoint;
    static Rectangle selection;
    private static BufferedImage screenShot;


    public static void main(String[] args) throws AWTException {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] ScreenDevices = ge.getScreenDevices();
        Rectangle totalBounds = new Rectangle();
        for (GraphicsDevice screenDevice : ScreenDevices) {
            totalBounds = totalBounds.union(screenDevice.getDefaultConfiguration().getBounds());
        }
        Robot robot = new Robot();

        screenShot = robot.createScreenCapture(new Rectangle(totalBounds)); // Tirando print da tela toda.

        // Criando o painel
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(screenShot, 0, 0, null);

                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (selection != null) {
                    g2.drawImage(
                            screenShot,
                            selection.x, selection.y,
                            selection.x + selection.width, selection.y + selection.height,
                            selection.x, selection.y,
                            selection.x + selection.width, selection.y + selection.height,
                            null);

                    g2.setColor(Color.red);
                    g2.drawRect(selection.x, selection.y, selection.width, selection.height);
                }
            }
        };
        JWindow janela = new JWindow();
        janela.add(panel);
        janela.setLocation(totalBounds.x, totalBounds.y);
        janela.setSize(totalBounds.width, totalBounds.height);
        janela.setVisible(true);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                endPoint = e.getPoint();
                updateSelection();
                panel.repaint();
            }

            public void mouseReleased(MouseEvent e) {
                endPoint = e.getPoint();
                updateSelection();
                panel.repaint();

                if (selection != null && selection.width > 0 && selection.height > 0) {
                    BufferedImage recorte = screenShot.getSubimage(
                            selection.x,
                            selection.y,
                            selection.width,
                            selection.height);

                    File arquivoRecorte = new File("C:/Users/danil/IdeaProjects/SnippingTranslate/temp/recorte.png");
                    try {
                        ImageIO.write(recorte, "png", arquivoRecorte);
                        janela.dispose();

                        OCRClient ocr = new OCRClient();
                        Dotenv dotenv = Dotenv.load();

                        String apiKey = dotenv.get("OCR_API_KEY");
                        String jsonResposta = ocr.parseImage(arquivoRecorte.getAbsolutePath(), apiKey, "eng");
                        translateText(jsonResposta);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Erro no processamento" + ex.getMessage());
                    }
                }
            }

        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                updateSelection();
                panel.repaint();
            }
        });

    }

    public static void updateSelection() {
        if (startPoint == null || endPoint == null)
            return;

        int x = Math.min(startPoint.x, endPoint.x);
        int y = Math.min(startPoint.y, endPoint.y);
        int width = Math.abs(startPoint.x - endPoint.x);
        int height = Math.abs(startPoint.y - endPoint.y);

        selection = new Rectangle(x, y, width, height);
    }

    public static String translateText(String text) {

        try {
            Dotenv dotenv = Dotenv.load();
            String authKey = dotenv.get("DEEPL_API_KEY");
            if (authKey == null || authKey.isBlank()) {
                throw new IllegalStateException("DEEPL_API_KEY não encontrada no arquivo .env");
            }
            DeepLClient cliente = new DeepLClient(authKey);
            TextResult result = cliente.translateText(text, null, "pt-BR");
            System.out.println(result.getText());
            String translated = result.getText();
            return translated;
        } catch (Exception e) {
            return "n foi possivel traduzir";
        }


    }
}
