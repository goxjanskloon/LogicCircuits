import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import github.goxjanskloon.swt.utils.MsgBox;
import github.goxjanskloon.logiccircuits.Board;
public class LogicCircuits implements Runnable{
    private static final Image[][] images={
    {new Image(null,"images/Void0.png"),new Image(null,"images/Void1.png")},
    {new Image(null,"images/Or0.png"),new Image(null,"images/Or1.png")},
    {new Image(null,"images/Not0.png"),new Image(null,"images/Not1.png")},
    {new Image(null,"images/And0.png"),new Image(null,"images/And1.png")},
    {new Image(null,"images/Xor0.png"),new Image(null,"images/Xor1.png")},
    {new Image(null,"images/Src0.png"),new Image(null,"images/Src1.png")}};
    private Board board;
    private Shell shell;
    private Display display;
    private Menu bar;
    private int xOffset=0,yOffset=0,blockSize=50,typeChoosed=0,xOrigin=0,yOrigin=0;
    private Board.Block blockChoosed=null;
    private String filePath="";
    public LogicCircuits(Display display,Board board,int width,int height){
        this.display=display;
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
                    break;
                case'o':
                    if(openFile()) MsgBox.open(shell,SWT.OK|SWT.ERROR,"Error","Error occured during opening.");
                    break;
                case'q':board.clear();shell.dispose();break;
                case'l':break;
                default:
                    if('1'<=ke.keyCode&&ke.keyCode<='6') typeChoosed=ke.keyCode-'1';
                    break;
                }
            }
            public void keyReleased(KeyEvent ke){
        }});
        shell.addMouseListener(new MouseListener(){
            public void mouseDoubleClick(MouseEvent e){}
            public void mouseDown(MouseEvent e){xOrigin=e.x;yOrigin=e.y;}
            public void mouseUp(MouseEvent e){
                if(e.x==xOrigin&&e.y==yOrigin) board.getBlock(e.x,e.y).clear();
            }
            
        });
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
    private void paint(Board.Block block){
        GC gc=new GC(shell);
        gc.drawImage(images[block.getType().ordinal()][block.getValue()?1:0],0,0,1000,1000,block.x*blockSize+xOffset,block.y*blockSize+yOffset,blockSize,blockSize);
        gc.dispose();
    }
    private boolean paint(){
        if(board.isEmpty()) return false;
        int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+shell.getSize().x/blockSize,yr=yl+shell.getSize().y/blockSize;
        if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.getWidth()) xr=board.getWidth();if(yr>=board.getHeight()) yr=board.getHeight();
        for(int i=0;i<=yr-yl;i++)
            for(int j=0;j<=xr-xl;j++) paint(board.getBlock(i,j));
        return true;
    }
    public void run(){
        for(shell.open();!shell.isDisposed();)
            if(!display.readAndDispatch()) display.sleep();
    }
    public static void main(String[] args){
        Display display=new Display();
        LogicCircuits boardUI=new LogicCircuits(display,new Board(),1000,600);
        boardUI.run();
        display.dispose();
    }
}
