import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
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
public class BoardUI extends JFrame{
    private class BlockModifyListener implements Board.ModifyListener<BlockModifyListener>{
        private static AtomicInteger count=new AtomicInteger(0);
        public final int id=count.getAndIncrement();
        public int compareTo(BlockModifyListener ml){return Integer.valueOf(id).compareTo(ml.id);}
        public void modifyBlock(Block block){
            int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+getWidth()/blockSize,yr=yl+getHeight()/blockSize;
            if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.getWidth()) xr=board.getWidth()-1;if(yr>=board.getHeight()) yr=board.getHeight()-1;
            if(block.x<xl||xr<block.x||block.y<yl||yr<block.y) return;
            paint(block);
        }
    }
    private enum OperationType{LINK,MOVE,SET_TYPE,NONE};
    private static BufferedImage[][] images=null;
    private static Color LINK_LC=new Color(125,125,125,125),LINK_RC=new Color(255,140,0,125);
    private Board board;
    private Graphics graphics=null;
    private int xOffset=0,yOffset=0,blockSize=50,choosedType=0,xOfsOrg=0,yOfsOrg=0;
    private File file=null;
    private OperationType operationType=OperationType.NONE;
    private Block choosedBlock=null;
    private boolean MMoved=false;
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
    public BoardUI(String title,GraphicsConfiguration gc,Rectangle bounds,Board board){
        super(title,gc);
        setBackground(Color.WHITE);
        graphics=getGraphics();
        setVisible(true);
        setBounds(bounds);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        board.addModifyListener(new BlockModifyListener());
        addKeyListener(new KeyListener(){
            public void keyPressed(KeyEvent ke){
                if(!ke.isControlDown()) return;
                switch(ke.getKeyChar()){
                case'n':{
                    Scanner s=new Scanner(System.in);
                    board.resetToSize(s.nextInt(),s.nextInt());
                    s.close();repaint();}break;
                case's':
                    if(ke.isShiftDown()?saveAsFile():saveFile()) JOptionPane.showMessageDialog(BoardUI.this,"Save failed!");
                    repaint();break;
                case'o':
                    if(openFile()) JOptionPane.showMessageDialog(BoardUI.this,"Open failed!");
                    repaint();break;
                case'l':operationType=OperationType.LINK;break;
                default:{char c=ke.getKeyChar();if('1'<=c&&c<='6') choosedType=c-'1';}break;
            }}
            public void keyReleased(KeyEvent ke){}
            public void keyTyped(KeyEvent ke){}
        });
        addMouseListener(new MouseListener(){
            public void mousePressed(MouseEvent me){}
            public void mouseEntered(MouseEvent me){}
            public void mouseExited(MouseEvent me){}
            public void mouseClicked(MouseEvent me){if(!board.isEmpty()){
                switch(me.getButton()){
                case MouseEvent.BUTTON1:
                    xOfsOrg=xOffset-me.getX();yOfsOrg=yOffset-me.getY();
                    operationType=OperationType.MOVE;
                    break;
                case MouseEvent.BUTTON2:break;
                case MouseEvent.BUTTON3:operationType=OperationType.SET_TYPE;break;
                default:break;
            }}}
            public void mouseReleased(MouseEvent me){
                switch(operationType){
                case LINK:{
                    Board.Block block=MToBlock(me.getX(),me.getY());
                    if(block!=null)
                        if(choosedBlock==null) choosedBlock=block;
                        else{choosedBlock.addOutput(block);operationType=OperationType.NONE;choosedBlock=null;}
                    }break;
                case MOVE:if(!MMoved){
                    Board.Block block=MToBlock(me.getX(),me.getY());
                    if(block!=null) block.clear();
                    }operationType=OperationType.NONE;MMoved=false;break;
                case SET_TYPE:if(!MMoved){
                    Board.Block block=MToBlock(me.getX(),me.getY());
                    if(block!=null)
                        if(block.getType()!=Block.Type.SRC) block.setType(Block.Type.valueOf(choosedType));
                        else block.inverseValue();
                    }operationType=OperationType.NONE;MMoved=false;break;
                case NONE:break;
                default:operationType=OperationType.NONE;break;
        }}});
        addMouseMotionListener(new MouseMotionListener(){
            public void mouseDragged(MouseEvent me){}
            public void mouseMoved(MouseEvent me){
                if(operationType==OperationType.MOVE){
                    xOffset=me.getX()+xOfsOrg;yOffset=me.getY()+yOfsOrg;
                    repaint();MMoved=true;
            }}});
    }
    private void paint(Block block){
        int x=block.x*blockSize+xOffset,y=block.y*blockSize+yOffset;
        if(x<-blockSize||getWidth()<x||y<-blockSize||getHeight()<y);
        graphics.drawImage(images[block.getType().ordinal()][block.getValue()?1:0],x,y,blockSize,blockSize,null);
        for(Board.Block target:block.getOutputs()){
            int tx=target.x*blockSize+xOffset,ty=target.y*blockSize+yOffset,mx=x+tx>>1,my=x+ty>>1;
            graphics.setColor(LINK_LC);
            graphics.drawLine(x,y,mx,my);
            graphics.setColor(LINK_RC);
            graphics.drawLine(mx,my,tx,ty);
        }
    }
    @Override
    public void paint(Graphics g){
        g.clearRect(getX(),getY(),getWidth(),getHeight());
        if(board==null||board.isEmpty()) return;
        int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+getWidth()/blockSize,yr=yl+getHeight()/blockSize;
        if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.getWidth()) xr=board.getWidth()-1;if(yr>=board.getHeight()) yr=board.getHeight()-1;
        for(int i=yl;i<=yr;i++)
            for(int j=xl;j<=xr;j++) paint(board.get(j,i));
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
        FileReader reader=null;
        try{reader=new FileReader(file);
        }catch(Exception e){e.printStackTrace();return false;}
        if(board.loadFrom(reader)){
            try{reader.close();
            }catch(Exception e){e.printStackTrace();return false;}
            return true;
        }
        try{reader.close();
        }catch(Exception e){e.printStackTrace();return false;}
        return false;
    }
    private boolean saveFile(){
        if(file==null) return saveAsFile();
        FileWriter writer=null;
        try{writer=new FileWriter(file);
        }catch(Exception e){e.printStackTrace();return false;}
        if(board.exportTo(writer)){
            try{writer.close();
            }catch(Exception e){e.printStackTrace();return false;}
            return true;
        }
        return false;
    }
    private boolean saveAsFile(){
        JFileChooser fc=new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(fc.showOpenDialog(fc)!=JFileChooser.APPROVE_OPTION){return false;}
        return saveFile();
    }
}
