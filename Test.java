import github.goxjanskloon.logiccircuits.Board;
import github.goxjanskloon.logiccircuits.Board.Block;
public class Test{
    public static void testOutput()throws Exception{
        Board board=new Board(10,10);
        Block src=board.get(0,0),tg=board.get(1,0);
        Thread.sleep(1);
        System.out.println(src.setType(Block.Type.SRC)+" "+src.addOutput(tg)+"\nsrc:"+src+" outputs:"+src.getOutputs()+"\ntg:"+tg+" inputs:"+src.getInputs());
    }
    public static void testOp()throws Exception{
        Board board=new Board(10,10);
        Block src1=board.get(0,0),src2=board.get(0,1),op=board.get(1,0),tg=board.get(1,1);
        src1.setType(Block.Type.SRC);
        src2.setType(Block.Type.SRC);
        op.setType(Block.Type.XOR);
        src1.addOutput(op);
        src2.addOutput(op);
        op.addOutput(tg);
        System.out.println(tg.getValue());
        src1.inverseValue();
        Thread.sleep(1);
        System.out.println(tg.getValue());
        src2.inverseValue();
        Thread.sleep(1);
        System.out.println(tg.getValue());
    }
    public static void main(String[] args)throws Exception{
        testOutput();
    }
}
