package github.goxjanskloon.logiccircuits;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.graphics.Image;
public class BoardUI implements Runnable{
    private static final Image[][] images={
        {new Image(null,"images/Void0.png"),new Image(null,"images/Void1.png")},
        {new Image(null,"images/Or0.png"),new Image(null,"images/Or1.png")},
        {new Image(null,"images/Not0.png"),new Image(null,"images/Not1.png")},
        {new Image(null,"images/And0.png"),new Image(null,"images/And1.png")},
        {new Image(null,"images/Xor0.png"),new Image(null,"images/Xor1.png")},
        {new Image(null,"images/Src0.png"),new Image(null,"images/Src1.png")}};
    private Board board;
    private Shell shell;
    private Menu bar;
    private int xOffset=0,yOffset=0,blockSize=50;
    public BoardUI(Display display,Board board,int width,int height){
        shell=new Shell(display);
        this.board=board;
        shell.setSize(width,height);
        bar=new Menu(shell,SWT.BAR);
        shell.setMenuBar(bar);
        shell.open();
    }
    void paint(Board.Block block){
        int x=block.x*blockSize+xOffset,y=block.y*blockSize+yOffset;
        Label label=new Label(shell,SWT.NONE);
        label.setImage(images[block.getType().ordinal()][block.getValue()?1:0]);
        label.setBounds(x,y,blockSize,blockSize);
    }
    boolean paint(){
        if(board.isEmpty()) return false;
        int xl=-xOffset/blockSize,yl=-yOffset/blockSize,xr=xl+shell.getSize().x/blockSize,yr=yl+shell.getSize().y/blockSize;
        if(xl<0) xl=0;if(yl<0) yl=0;if(xr>=board.getWidth()) xr=board.getWidth();if(yr>=board.getHeight()) yr=board.getHeight();
        for(int i=0;i<=yr-yl;i++)
            for(int j=0;j<=xr-xl;j++) paint(board.blocks.get(i).get(j));
        return true;
    }
    public void run(){
    }
}
