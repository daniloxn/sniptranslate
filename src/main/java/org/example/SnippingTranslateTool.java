package org.example;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class SnippingTranslateTool{


    static Point startPoint;
    static Point endPoint;
    static Rectangle selection;
    private static BufferedImage screenShot;




    public static void main(String[] args) throws AWTException, IOException {
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
                            null
                    );

                    g2.setColor(Color.red);
                    g2.drawRect(selection.x, selection.y, selection.width, selection.height);
                }
           }
       };
        JWindow janela = new JWindow();
        janela.add(panel);
        janela.setSize(totalBounds.width, totalBounds.height);
        janela.setLocation(totalBounds.x, totalBounds.y);
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

           if ( selection != null && selection.width > 0 && selection.height > 0 ) {
               BufferedImage recorte = screenShot.getSubimage(
                       selection.x,
                       selection.y,
                       selection.width,
                       selection.height
               );

               try {
                   ImageIO.write(recorte, "png", new File("recorte.png"));
                   janela.dispose();
               } catch (IOException ex) {
                   throw new RuntimeException(ex);
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
        if (startPoint == null || endPoint == null) return;

        int x = Math.min(startPoint.x, endPoint.x);
        int y = Math.min(startPoint.y, endPoint.y);
        int width = Math.abs(startPoint.x - endPoint.x);
        int height = Math.abs(startPoint.y - endPoint.y);

        selection = new Rectangle(x, y, width, height);
    }
}
