import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import github.goxjanskloon.logiccircuits.Board;
import github.goxjanskloon.logiccircuits.Board.Block;
public class BoardFrame extends JFrame{
    private class BlockModifyListener implements Board.ModifyListener<BlockModifyListener>{
        private static AtomicInteger count=new AtomicInteger(0);
        public final int id=count.getAndIncrement();
        public int compareTo(BlockModifyListener ml){return Integer.valueOf(id).compareTo(ml.id);}
        public void modifyBlock(Block block){
            int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+getWidth()/blockSize,yr=yl+getHeight()/blockSize;
            if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.getWidth()) xr=board.getWidth()-1;if(yr>=board.getHeight()) yr=board.getHeight()-1;
            if(block.x<xl||xr<block.x||block.y<yl||yr<block.y) return;
            paint(block);
    }}
    private static BufferedImage[][] IMAGES=null;
    private static Color LINK_LC=new Color(125,125,125,64),LINK_RC=new Color(255,140,0,64);
    private Board board;
    private Graphics graphics=null;
    private int xOffset=0,yOffset=0,blockSize=50,choosedType=0,xOfsOrg=0,yOfsOrg=0;
    private File file=null;
    private Block choosedBlock=null;
    private static BufferedImage readImage(String path)throws IOException{
        return ImageIO.read(Board.class.getClassLoader().getResource(path));
    }
    static{try{
        IMAGES=new BufferedImage[][]{
        {readImage("img/VOID0.png"),readImage("img/VOID1.png")},
        {readImage("img/OR0.png"),readImage("img/OR1.png")},
        {readImage("img/NOT0.png"),readImage("img/NOT1.png")},
        {readImage("img/AND0.png"),readImage("img/AND1.png")},
        {readImage("img/XOR0.png"),readImage("img/XOR1.png")},
        {readImage("img/SRC0.png"),readImage("img/SRC1.png")}};
        }catch(IOException e){e.printStackTrace();}}
    public BoardFrame(){
        super();
        this.board=new Board();
        board.addModifyListener(new BlockModifyListener());
        addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent ke){
                if(!ke.isControlDown()) return;
                switch(ke.getKeyCode()){
                case'N':{
                    Scanner s=new Scanner(System.in);
                    board.resetWithSize(s.nextInt(),s.nextInt());
                    s.close();repaint();}break;
                case'S':
                    if(ke.isShiftDown()?!saveAsFile():!saveFile()) JOptionPane.showMessageDialog(BoardFrame.this,"Save failed!");
                    repaint();break;
                case'O':
                    if(!openFile()) JOptionPane.showMessageDialog(BoardFrame.this,"Open failed!");
                    repaint();break;
                case'Q':blockSize+=10;repaint();break;
                case'E':if(blockSize>10){blockSize-=10;repaint();}break;
                case'X':if(!board.isEmpty()){board.clear();repaint();}break;
                default:{int c=ke.getKeyCode();if('1'<=c&&c<='6')choosedType=c-'1';}break;
        }}});
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent me){if(!board.isEmpty()){
                xOfsOrg=xOffset-me.getX();yOfsOrg=yOffset-me.getY();}}
            @Override
            public void mouseClicked(MouseEvent me){if(!board.isEmpty()){
                switch(me.getButton()){
                case MouseEvent.BUTTON1:{
                    Board.Block block=MToBlock(me.getX(),me.getY());
                    if(block!=null) block.clear();}break;
                case MouseEvent.BUTTON2:{
                    Board.Block block=MToBlock(me.getX(),me.getY());
                    if(choosedBlock!=null){
                        choosedBlock.addOutput(block);
                        choosedBlock=null;
                    }else choosedBlock=block;
                }break;
                case MouseEvent.BUTTON3:{
                    Board.Block block=MToBlock(me.getX(),me.getY());if(block!=null)
                    if(block.getType()==Block.Type.SRC) block.inverseValue();
                    else block.setType(Block.Type.valueOf(choosedType));}break;
                default:break;
        }}}});
        addMouseMotionListener(new MouseAdapter(){
            @Override
            public void mouseDragged(MouseEvent me){if(!board.isEmpty()){
                xOffset=me.getX()+xOfsOrg;yOffset=me.getY()+yOfsOrg;repaint();}}});
    }
    @Override
    public void setVisible(boolean visible){
        super.setVisible(visible);
        if(visible){
            graphics=getGraphics();
            ((Graphics2D)graphics).setStroke(new BasicStroke(10));
        }
    }
    private void paint(Block block,Graphics g){
        int x=block.x*blockSize+xOffset,y=block.y*blockSize+yOffset;
        if(x<-blockSize||getWidth()<=x||y<-blockSize||getHeight()<=y) return;
        g.drawImage(IMAGES[block.getType().ordinal()][block.getValue()?1:0],x,y,blockSize,blockSize,null);
        x+=blockSize/2;y+=blockSize/2;
        for(Board.Block target:block.getOutputs()){
            int tx=target.x*blockSize+xOffset+blockSize/2,
                ty=target.y*blockSize+yOffset+blockSize/2,mx=x+tx>>1,my=y+ty>>1;
            graphics.setColor(LINK_LC);
            graphics.drawLine(x,y,mx,my);
            graphics.setColor(LINK_RC);
            graphics.drawLine(mx,my,tx,ty);
        }
    }
    private void paint(Block block){paint(block,graphics);}
    @Override
    public void paint(Graphics g){
        if(board==null||board.isEmpty()){g.clearRect(getX(),getY(),getWidth(),getHeight());return;};
        Image image=createImage(getWidth(),getHeight());
        Graphics ig=image.getGraphics();
        int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+getWidth()/blockSize,yr=yl+getHeight()/blockSize;
        if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.getWidth()) xr=board.getWidth()-1;if(yr>=board.getHeight()) yr=board.getHeight()-1;
        for(int i=yl;i<=yr;i++)
            for(int j=xl;j<=xr;j++) paint(board.get(j,i),ig);
        g.drawImage(image,0,0,null);
    }
    private Board.Block MToBlock(int x,int y){
        x-=xOffset;y-=yOffset;
        x/=blockSize;y/=blockSize;
        if(x<0||board.getWidth()<=x||y<0||board.getHeight()<=y) return null;
        return board.get(x,y);
    }
    private boolean openFile(){
        JFileChooser fc=new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(fc.showOpenDialog(fc)!=JFileChooser.APPROVE_OPTION){return false;}
        file=fc.getSelectedFile();
        FileReader reader=null;
        try{reader=new FileReader(file);
        }catch(FileNotFoundException e){e.printStackTrace();return false;}
        if(board.loadFrom(reader)){
            try{reader.close();
            }catch(IOException e){e.printStackTrace();return false;}
            return true;
        }try{reader.close();
        }catch(IOException e){e.printStackTrace();return false;}
        return false;
    }
    private boolean saveFile(){
        if(file==null) return saveAsFile();
        FileWriter writer=null;
        try{writer=new FileWriter(file);
        }catch(IOException e){e.printStackTrace();return false;}
        if(board.exportTo(writer)){
            try{writer.close();
            }catch(IOException e){e.printStackTrace();return false;}
            return true;
        }return false;
    }
    private boolean saveAsFile(){
        JFileChooser fc=new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(fc.showOpenDialog(fc)!=JFileChooser.APPROVE_OPTION){return false;}
        file=fc.getSelectedFile();
        return saveFile();
    }
    public static void main(String[] args){
        BoardFrame bf=new BoardFrame();
        bf.setTitle("LogicCircuits");
        bf.setSize(1000,600);
        bf.setBackground(Color.WHITE);
        bf.setDefaultCloseOperation(EXIT_ON_CLOSE);
        bf.setVisible(true);
    }
}
