import java.awt.Color;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFrame;
import github.goxjanskloon.logiccircuits.Board;
import github.goxjanskloon.logiccircuits.Board.Block;
class BoardUI extends JFrame{
    Container container=null;
    public BoardUI(String title,GraphicsConfiguration gc,Rectangle bouns){
        super(title,gc);
        (container=getContentPane()).setBackground(Color.WHITE);
        setVisible(true);
        setBounds(bouns);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
public class LogicCircuits{
    private class BoardModifyListener implements ModifyListener<BoardModifyListener>{
        private static AtomicInteger count=new AtomicInteger(0);
        public final int id=count.getAndIncrement();
        public int compareTo(BoardModifyListener ml){return Integer.valueOf(id).compareTo(ml.id);}
        public void modifyBlock(Block block){
            int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+shell.getSize().x/blockSize,yr=yl+shell.getSize().y/blockSize;
            if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.getWidth()) xr=board.getWidth()-1;if(yr>=board.getHeight()) yr=board.getHeight()-1;
            if(block.x<xl||xr<block.x||block.y<yl||yr<block.y) return;
            GC gc=new GC(shell);
            paint(block,gc);gc.dispose();
        }
    }
    private enum OperationType{LINK,MOVE,SET_TYPE,NONE};
    private static final Image[][] images={
    {new Image(null,"images/VOID0.png"),new Image(null,"images/VOID1.png")},
    {new Image(null,"images/OR0.png"),new Image(null,"images/OR1.png")},
    {new Image(null,"images/NOT0.png"),new Image(null,"images/NOT1.png")},
    {new Image(null,"images/AND0.png"),new Image(null,"images/AND1.png")},
    {new Image(null,"images/XOR0.png"),new Image(null,"images/XOR1.png")},
    {new Image(null,"images/SRC0.png"),new Image(null,"images/SRC1.png")}};
    private Board board;
    private JFrame frame;
    private int xOffset=0,yOffset=0,blockSize=50,choosedType=0,xOfsOrg=0,yOfsOrg=0;
    private Block choosedBlock=null;
    private String filePath="";
    private OperationType operationType=OperationType.NONE;
    private boolean MMoved=false;
    public LogicCircuits(Display display,Board board,int width,int height){
        shell=new Shell(display);
        this.board=board;
        shell.setSize(width,height);
        bar=new Menu(shell,SWT.BAR);
        shell.setMenuBar(bar);
        board.addModifyListener(new BoardModifyListener());
        shell.addKeyListener(new KeyListener(){
            public void keyPressed(KeyEvent ke){
                if((ke.stateMask&SWT.CTRL)==0) return;
                switch(ke.keyCode){
                case'n':{
                    Scanner s=new Scanner(System.in);
                    board.resetToSize(s.nextInt(),s.nextInt());
                    s.close();
                    paint();
                }break;
                case's':
                    if(!((ke.stateMask&SWT.SHIFT)!=0?saveAsFile():saveFile())) MsgBox.open(shell,SWT.OK|SWT.ERROR,"Error","Error occured during saving.");
                    paint();break;
                case'o':
                    if(openFile()) MsgBox.open(shell,SWT.OK|SWT.ERROR,"Error","Error occured during opening.");
                    paint();break;
                case'q':board.clear();shell.dispose();paint();break;
                case'l':operationType=OperationType.LINK;break;
                default:if('1'<=ke.keyCode&&ke.keyCode<='6') choosedType=ke.keyCode-'1';break;
            }}
            public void keyReleased(KeyEvent ke){}
        });
        shell.addMouseListener(new MouseListener(){
            public void mouseDoubleClick(MouseEvent me){}
            public void mouseDown(MouseEvent me){if(!board.isEmpty()){
                switch(me.button){
                case 1:
                    xOfsOrg=xOffset-me.x;yOfsOrg=yOffset-me.y;
                    operationType=OperationType.MOVE;
                    break;
                case 2:break;
                case 3:operationType=OperationType.SET_TYPE;break;
                default:break;
            }}}
            public void mouseUp(MouseEvent me){
                switch(operationType){
                case LINK:{
                    Board.Block block=MToBlock(me.x,me.y);
                    if(block!=null)
                        if(choosedBlock==null) choosedBlock=block;
                        else{choosedBlock.addOutput(block);operationType=OperationType.NONE;choosedBlock=null;}
                    }break;
                case MOVE:if(!MMoved){
                    Board.Block block=MToBlock(me.x,me.y);
                    if(block!=null) block.clear();
                    }operationType=OperationType.NONE;MMoved=false;break;
                case SET_TYPE:if(!MMoved){
                    Board.Block block=MToBlock(me.x,me.y);
                    if(block!=null)
                        if(block.getType()!=Block.Type.SRC) block.setType(Block.Type.valueOf(choosedType));
                        else block.inverseValue();
                    }operationType=OperationType.NONE;MMoved=false;break;
                case NONE:break;
                default:operationType=OperationType.NONE;break;
                }
        }});
        shell.addMouseMoveListener(new MouseMoveListener(){
            public void mouseMove(MouseEvent me){
                if(operationType==OperationType.MOVE){
                    xOffset=me.x+xOfsOrg;yOffset=me.y+yOfsOrg;
                    paint();MMoved=true;
        }}});
    }
    private Board.Block MToBlock(int x,int y){
        x-=xOffset;y-=yOffset;
        x/=blockSize;y/=blockSize;
        if(x<0||board.getWidth()<=x||y<0||board.getHeight()<=y) return null;
        return board.get(x,y);
    }
    private boolean openFile(){
        filePath=(new FileDialog(shell)).open();
        FileReader reader=null;
        try{reader=new FileReader(filePath);
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
        if(filePath.isEmpty()) return saveAsFile();
        FileWriter writer=null;
        try{writer=new FileWriter(filePath);
        }catch(Exception e){e.printStackTrace();return false;}
        if(board.exportTo(writer)){
            try{writer.close();
            }catch(Exception e){e.printStackTrace();return false;}
            return true;
        }
        return false;
    }
    private boolean saveAsFile(){
        filePath=(new FileDialog(shell)).open();
        return saveFile();
    }
    private void paint(Board.Block block,GC gc){
        int x=block.x*blockSize+xOffset,y=block.y*blockSize+yOffset;
        gc.drawImage(images[block.getType().ordinal()][block.getValue()?1:0],0,0,16,16,x,y,blockSize,blockSize);
        for(Board.Block target:block.getOutputs()){
            int tx=target.x*blockSize+xOffset,ty=target.y*blockSize+yOffset,mx=x+tx>>1,my=x+ty>>1;
            gc.setForeground(new Color(255,255,255,125));
            gc.drawLine(x,y,mx,my);
            gc.setForeground(new Color(255,140,0,125));
            gc.drawLine(mx,my,tx,ty);
        }
    }
    private boolean paint(){
        GC gc=new GC(shell);
        gc.setForeground(new Color(255,255,255));
        gc.fillRectangle(shell.getClientArea());
        if(board.isEmpty()){gc.dispose();return false;}
        int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+shell.getSize().x/blockSize,yr=yl+shell.getSize().y/blockSize;
        if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.getWidth()) xr=board.getWidth()-1;if(yr>=board.getHeight()) yr=board.getHeight()-1;
        for(int i=yl;i<=yr;i++)
            for(int j=xl;j<=xr;j++) paint(board.get(j,i),gc);
        gc.dispose();
        return true;
    }
    public void run(){
        for(shell.open();!shell.isDisposed();)
            if(!shell.getDisplay().readAndDispatch()) shell.getDisplay().sleep();
    }
    public static void main(String[] args){
        LogicCircuits boardUI=new LogicCircuits(Display.getDefault(),new Board(),1000,600);
        boardUI.run();
    }
}
