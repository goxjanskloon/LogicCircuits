import github.goxjanskloon.logiccircuits.Board;
import github.goxjanskloon.logiccircuits.Board.Block;
public class Test{
    public static void main(String[] args)throws Exception{
        Board board=new Board(10,10);
        Block src1=board.getBlock(0,0),src2=board.getBlock(0,1),op=board.getBlock(1,0),tg=board.getBlock(1,1);
        src1.setType(Block.Type.SRC.ordinal());
        src2.setType(Block.Type.SRC.ordinal());
        op.setType(Block.Type.XOR.ordinal());
        src1.linkTo(op);
        src2.linkTo(op);
        op.linkTo(tg);
        System.out.println(tg.getValue());
        src1.exchangeValue();
        Thread.sleep(1);
        System.out.println(tg.getValue());
        src2.exchangeValue();
        Thread.sleep(1);
        System.out.println(tg.getValue());
    }
}
