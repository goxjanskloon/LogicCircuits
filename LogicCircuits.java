import java.io.FileReader;
import java.io.FileWriter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Color;
import github.goxjanskloon.swt.utils.MsgBox;
import github.goxjanskloon.logiccircuits.Board;
public class LogicCircuits/*  implements Runnable*/{
    private enum OperationType{LINK,MOVE,SET_TYPE,NONE};
    private static final Image[][] images={
    {new Image(null,"images/VOID0.png"),new Image(null,"images/VOID1.png")},
    {new Image(null,"images/OR0.png"),new Image(null,"images/OR1.png")},
    {new Image(null,"images/NOT0.png"),new Image(null,"images/NOT1.png")},
    {new Image(null,"images/AND0.png"),new Image(null,"images/AND1.png")},
    {new Image(null,"images/XOR0.png"),new Image(null,"images/XOR1.png")},
    {new Image(null,"images/SRC0.png"),new Image(null,"images/SRC1.png")}};
    private Board board;
    private Shell shell;
    private Menu bar;
    private int xOffset=0,yOffset=0,blockSize=50,choosedType=0,xOfsOrg=0,yOfsOrg=0;
    private Board.Block choosedBlock=null;
    private String filePath="";
    private OperationType operationType=OperationType.NONE;
    private boolean MMoved=false;
    public LogicCircuits(Display display,Board board,int width,int height){
        shell=new Shell(display);
        this.board=board;
        shell.setSize(width,height);
        bar=new Menu(shell,SWT.BAR);
        shell.setMenuBar(bar);
        shell.addKeyListener(new KeyListener(){
            public void keyPressed(KeyEvent ke){
                if((ke.stateMask&SWT.CTRL)==0) return;
                switch(ke.keyCode){
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
            public void mouseDown(MouseEvent me){
                switch(me.button){
                case 1:
                    xOfsOrg=xOffset+me.x;yOfsOrg=yOffset+me.y;
                    operationType=OperationType.MOVE;
                    break;
                case 2:break;
                case 3:operationType=OperationType.SET_TYPE;break;
                default:break;
            }}
            public void mouseUp(MouseEvent me){
                switch(operationType){
                case LINK:{
                    Board.Block block=MToBlock(me.x,me.y);
                    if(choosedBlock==null) choosedBlock=block;
                    else if(block!=null){choosedBlock.linkTo(block);operationType=OperationType.NONE;}
                    }break;
                case MOVE:if(!MMoved){
                    Board.Block block=board.getBlock(me.x,me.y);
                    if(block!=null) block.clear();
                    }operationType=OperationType.NONE;break;
                case SET_TYPE:if(!MMoved){
                    Board.Block block=board.getBlock(me.x,me.y);
                    if(block!=null) block.setType(choosedType);
                    }operationType=OperationType.NONE;break;
                case NONE:break;
                default:operationType=OperationType.NONE;break;
                }
        }});
        shell.addMouseMoveListener(new MouseMoveListener(){
            public void mouseMove(MouseEvent me){
                if(operationType==OperationType.MOVE){
                    xOffset=xOfsOrg-me.x;yOffset=yOfsOrg-me.y;
                    paint();
        }}});
    }
    private Board.Block MToBlock(int x,int y){
        x-=xOffset;y-=yOffset;
        x/=blockSize;y/=blockSize;
        if(x<0||board.getWidth()<x||y<0||board.getHeight()<y) return null;
        return board.getBlock(x,y);
    }
    private boolean openFile(){
        filePath=(new DirectoryDialog(shell)).open();
        FileReader reader=null;
        try{reader=new FileReader(filePath);
        }catch(Exception e){e.printStackTrace();return false;}
        if(board.loadFrom(reader)){
            try{reader.close();
            }catch(Exception e){e.printStackTrace();return false;}
            return true;
        }
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
        filePath=(new DirectoryDialog(shell)).open();
        return saveFile();
    }
    private void paint(Board.Block block,GC gc){
        int x=block.x*blockSize+xOffset,y=block.y*blockSize+yOffset;
        gc.drawImage(images[block.getType().ordinal()][block.getValue()?1:0],0,0,1000,1000,x,y,blockSize,blockSize);
        for(Board.Block target:block.getOutputBlocks()){
            int tx=target.x*blockSize+xOffset,ty=target.y*blockSize+yOffset,mx=x+tx>>1,my=x+ty>>1;
            gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_BLUE));
            gc.drawLine(x,y,mx,my);
            gc.setForeground(new Color(null,255,140,0));
            gc.drawLine(mx,my,tx,ty);
        }
    }
    private boolean paint(){
        GC gc=null;
        if(board.isEmpty()){
            gc=new GC(shell);
            gc.setForeground(new Color(0,0,0));
            gc.fillRectangle(shell.getBounds());
            gc.dispose();
            return false;
        }
        int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+shell.getSize().x/blockSize,yr=yl+shell.getSize().y/blockSize;
        if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.getWidth()) xr=board.getWidth();if(yr>=board.getHeight()) yr=board.getHeight();
        gc=new GC(shell);
        for(int i=0;i<=yr-yl;i++)
            for(int j=0;j<=xr-xl;j++) paint(board.getBlock(i,j),gc);
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
        //display.dispose();
    }
}
