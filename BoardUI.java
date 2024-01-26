import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import github.goxjanskloon.logiccircuits.Board.Block;
public class BoardUI extends JFrame{
    private Container container=null;
    private Graphics graphics=null;
    private int xOffset=0,yOffset=0,blockSize=50;
    public BoardUI(String title,GraphicsConfiguration gc,Rectangle bouns){
        super(title,gc);
        (container=getContentPane()).setBackground(Color.WHITE);
        graphics=getGraphics();
        setVisible(true);
        setBounds(bouns);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    private static final BufferedImage[][] images;
    static{try{
        images=new BufferedImage[][]{
        {ImageIO.read(new File("img/VOID0.png")),
         ImageIO.read(new File("img/VOID1.png"))},
        {ImageIO.read(new File("img/OR0.png")),
         ImageIO.read(new File("img/OR1.png"))},
        {ImageIO.read(new File("img/NOT0.png")),
         ImageIO.read(new File("img/NOT1.png"))},
        {ImageIO.read(new File("img/AND0.png")),
         ImageIO.read(new File("img/AND1.png"))},
        {ImageIO.read(new File("img/XOR0.png")),
         ImageIO.read(new File("img/XOR1.png"))},
        {ImageIO.read(new File("img/SRC0.png")),
         ImageIO.read(new File("img/SRC1.png"))}
        };}catch(IOException e){e.printStackTrace();}}
    private void paint(Block block){
        int x=block.x*blockSize+xOffset,y=block.y*blockSize+yOffset;
        graphics.drawImage(images[block.getType().ordinal()][block.getValue()?1:0],x,y,blockSize,blockSize,);
    }
}
