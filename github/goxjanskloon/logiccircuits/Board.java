package github.goxjanskloon.logiccircuits;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class Board{
    public interface ModifyListener{void modifyBlock(Block block);}
    public class Block{
        public enum Type{
            VOID(),OR(),NOT(),AND(),XOR(),SRC();
            public static Type valueOf(int value){
                switch(value){
                case 0:return VOID;
                case 1:return OR;
                case 2:return NOT;
                case 3:return AND;
                case 4:return XOR;
                case 5:return SRC;
                default:return null;
        }}}
        public final int x,y;
        private AtomicInteger type;
        private AtomicBoolean value;
        private Block[] input=new Block[2];
        private ConcurrentSkipListSet<Block> output=new ConcurrentSkipListSet<Block>();
        private Block(Type type,boolean value,int x,int y){
            this.x=x;this.y=y;
            this.type=new AtomicInteger(type.ordinal());
            this.value=new AtomicBoolean(value);
        }
        public Type getType(){return Type.valueOf(type.get());}
        public boolean setType(Type type){
            if(getType()==type) return false;
            this.type.set(type.ordinal());
            flush();callModifyListeners(this);
            return true;
        }
        public boolean getValue(){return value.get();}
        public boolean inverseValue(){
            if(getType()!=Type.SRC) throw new UnsupportedOperationException("Calling exchangeValue() on a not SRC-Type Block");
            boolean result=!getValue();value.getAndSet(result);
            for(Block block:output) block.flush();
            return result;
        }
        public int getInputSize(){return (input[0]==null?0:1)+(input[1]==null?0:1);}
        public int getOutputSize(){return output.size();}
        public Block[] getInputs(){
            Block[] result=new Block[getInputSize()];
            switch(result.length){
            case 2:result[0]=input[0];result[1]=input[1];break;
            case 1:result[0]=(input[0]!=null?input[0]:input[1]);break;
            case 0:break;
            default:break;
            }return result;
        }
        public Block[] getOutputs(){return (Block[]) output.toArray();}
        private boolean addInput(Block block){
            switch(getType()){
            case VOID:
            case NOT:if(getInputSize()==1) return false;else break;
            case OR:
            case AND:
            case XOR:if(getInputSize()==2) return false;else break;
            case SRC:
            default:return false;
            }
            input[getInputSize()]=block;return true;
        }
        public boolean addOutput(Block block){
            if(output.contains(block)||block.addInput(this)) return false;
            output.add(block);
            block.flush();callModifyListeners(this);
            return true;
        }
        private boolean removeInput(Block block){
            if(input[1]!=null){input[1]=null;return true;}
            else if(input[0]!=null){input[0]=null;return true;}
            else return false;
        }
        public boolean removeOutput(Block block){return output.remove(block);}
        private boolean clearInput(){
            if(getInputSize()==0) return false;
            input[0]=input[1]=null;
            return true;
        }
        public boolean clearOutput(){
            if(output.isEmpty()) return false;
            output.clear();return true;
        }
        public void flush(){
            threadPool.execute(new Runnable(){public void run(){
            boolean newValue=false;
            int len=getInputSize();
            switch(getType()){
            case SRC:newValue=getValue();break;
            case VOID:newValue=len==1&&input[0].getValue();break;
            case NOT:newValue=len==1&&!input[0].getValue();
            case OR:newValue=len==2&&input[0]!=null&&input[0].getValue()||input[1].getValue();break;
            case AND:newValue=len==2&&input[0].getValue()&&input[1].getValue();break;
            case XOR:newValue=len==2&&input[0].getValue()^input[1].getValue();break;
            default:break;
            }
            if(value.compareAndSet(!newValue,newValue)){
                for(Block b:output) b.flush();
                callModifyListeners(Block.this);
        }}});}
        public boolean isEmpty(){return getType()==Type.VOID&&getInputSize()==0&&output.isEmpty();}
        public boolean clear(){
            if(isEmpty()) return false;
            for(Block block:input) block.removeOutput(this);
            for(Block block:output){block.removeInput(this);block.flush();}
            clearInput();clearOutput();setType(Type.VOID);
            callModifyListeners(this);
            return true;
        }
    }
    private ArrayList<ArrayList<Block>> blocks=new ArrayList<ArrayList<Block>>();
    private ConcurrentSkipListSet<ModifyListener> modifyListeners=new ConcurrentSkipListSet<ModifyListener>();
    private ExecutorService threadPool=Executors.newCachedThreadPool(new ThreadFactory(){
        public Thread newThread(Runnable r){
            Thread thread=new Thread(r);
            thread.setDaemon(true);
            return thread;
        }});
    public Board(){}
    public Board(int width,int height){resetToSize(width, height);}
    public boolean addModifyListener(ModifyListener modifyListener){return modifyListeners.add(modifyListener);}
    public boolean removeModifyListener(ModifyListener modifyListener){return modifyListeners.remove(modifyListener);}
    public boolean clearModifyListeners(){
        if(modifyListeners.isEmpty()) return false;
        modifyListeners.clear();return true;
    }
    private void callModifyListeners(Block block){for(ModifyListener ml:modifyListeners) ml.modifyBlock(block);}
    public Block get(int x,int y){return blocks.get(x).get(y);}
    public boolean isEmpty(){return blocks.isEmpty();}
    public int getWidth(){return isEmpty()?0:blocks.getFirst().size();}
    public int getHeight(){return blocks.size();}
    public boolean clear(){
        if(isEmpty()) return false;
        silence();blocks.clear();
        return true;
    }
    public void silence(){threadPool.shutdownNow();}
    public boolean loadFrom(Readable reader){
        clear();try{
        Scanner scanner=new Scanner(reader);
        int width=scanner.nextInt(),height=scanner.nextInt();
        for(int i=0;i<height;i++){
            blocks.add(new ArrayList<Block>());
            for(int j=0;j<width;j++){
                blocks.getLast().add(new Block(Block.Type.valueOf(scanner.nextInt()),scanner.nextInt()==1,i,j));
            }
        }
        for(int i=0;i<height;i++)
            for(int j=0;j<width;j++){
                Block block=get(i,j);
                for(int inputSize=scanner.nextInt();inputSize-->0;) block.addInput(get(scanner.nextInt(),scanner.nextInt()));
                for(int outputSize=scanner.nextInt();outputSize-->0;) block.addOutput(get(scanner.nextInt(),scanner.nextInt()));
            }
        scanner.close();
        }catch(Exception e){
            e.printStackTrace();
            clear();
            return false;
        }
        return true;
    }
    public boolean exportTo(Writer writer){try{
        writer.write(blocks.size()+" "+blocks.getFirst().size()+" ");
        for(int i=0;i<blocks.size();i++)
            for(int j=0;j<blocks.get(i).size();j++){
                Block block=get(i,j);
                writer.write(block.type.get()+" "+(block.value.get()?1:0)+" ");
            }
        for(int i=0;i<blocks.size();i++)
            for(int j=0;j<blocks.get(i).size();j++){
                Block block=get(i,j);
                writer.write(block.getInputSize()+" "+block.getOutputSize()+" ");
                for(Block b:block.getInputs()) writer.write(b.x+" "+b.y+" ");
                for(Block b:block.getOutputs()) writer.write(b.x+" "+b.y+" ");
            }
        }catch(Exception e){e.printStackTrace();return false;}
        return true;
    }
    public void resetToSize(int width,int height){
        clear();
        for(int i=0;i<height;i++){
            blocks.add(new ArrayList<Block>());
            for(int j=0;j<width;j++) blocks.getLast().add(new Block(Block.Type.VOID,false,i,j));
        }
    }
}
