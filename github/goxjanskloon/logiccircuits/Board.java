package github.goxjanskloon.logiccircuits;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class Board{
    public class Block{
        public enum Type{
            Void(),Or(),Not(),And(),Xor(),Src();
            public static Type valueOf(int value){
                switch(value){
                case 0:return Void;
                case 1:return Or;
                case 2:return Not;
                case 3:return And;
                case 4:return Xor;
                case 5:return Src;
                default:return null;
                }
            }
        }
        public final int x,y;
        private AtomicInteger type;
        private AtomicBoolean value;
        private CopyOnWriteArrayList<Block> input,output;
        private Block(int type,boolean value,int x,int y){
            this.x=x;this.y=y;
            this.type=new AtomicInteger(type);
            this.value=new AtomicBoolean(value);
        }
        public Type getType(){return Type.valueOf(type.get());}
        public boolean getValue(){return value.get();}
        public void flush(){
            threadPool.execute(new Runnable(){public void run(){
            boolean newValue;
            switch(Block.Type.valueOf(type.get())){
            case Void:newValue=!input.isEmpty()&&input.getFirst().value.get();break;
            case Or:newValue=input.size()>=2&&(input.getFirst().value.get()||input.get(1).value.get());break;
            case Not:newValue=!input.isEmpty()&&!input.getFirst().value.get();break;
            case And:newValue=input.size()>=2&&input.getFirst().value.get()&&input.get(1).value.get();break;
            case Xor:newValue=input.size()>=2&&input.getFirst().value.get()^input.get(1).value.get();break;
            case Src:newValue=value.get();break;
            default:newValue=false;
            }
            if(value.compareAndSet(!newValue,newValue))
                for(Block b:output) b.flush();
        }});}
        public boolean linkTo(Block block){
            if(output.contains(block)) return false;
            output.add(block);
            block.input.add(this);
            return true;
        }
        public boolean exchangeValue(){
            if(getType()!=Type.Src) throw new UnsupportedOperationException("Calling exchangeValue() on a not Src-Type Block");
            return !value.getAndSet(!getValue());
        }
        public boolean isEmpty(){
            return getType()==Type.Void&&input.isEmpty()&&output.isEmpty();
        }
        public boolean clear(){
            if(isEmpty()) return false;
            input.clear();output.clear();type.set(Type.Void.ordinal());
            return true;
        }
    }
    private ArrayList<ArrayList<Block>> blocks;
    private ExecutorService threadPool=Executors.newCachedThreadPool(new ThreadFactory(){
        public Thread newThread(Runnable r){return new Thread(r);}
    });
    public boolean isEmpty(){return blocks.isEmpty();}
    public int getWidth(){return blocks.size();}
    public int getHeight(){return isEmpty()?0:blocks.getFirst().size();}
    public boolean clear(){
        if(isEmpty()) return false;
        threadPool.shutdownNow();
        blocks.clear();
        return true;
    }
    public void silence(){threadPool.shutdownNow();}
    public boolean loadFile(File file){
        if(!blocks.isEmpty()) clear();
        FileReader fileReader=null;
        try{
            fileReader=new FileReader(file);
            Scanner scanner=new Scanner(fileReader);
            int width=scanner.nextInt(),height=scanner.nextInt();
            for(int i=0;i<height;i++){
                blocks.add(new ArrayList<Block>());
                for(int j=0;j<width;j++){
                    blocks.getLast().add(new Block(scanner.nextInt(),scanner.nextInt()==1,i,j));
                }
            }
            for(int i=0;i<height;i++)
                for(int j=0;j<width;j++){
                    Block block=blocks.get(i).get(j);
                    for(int inputSize=scanner.nextInt();inputSize-->0;) block.input.add(blocks.get(scanner.nextInt()).get(scanner.nextInt()));
                    for(int outputSize=scanner.nextInt();outputSize-->0;) block.output.add(blocks.get(scanner.nextInt()).get(scanner.nextInt()));
                }
            scanner.close();
        }
        catch(Exception e){
            e.printStackTrace();
            clear();
            return false;
        }
        finally{
            try{fileReader.close();}
            catch(Exception e){e.printStackTrace();}
        }
        return true;
    }
    public boolean exportFile(File file){
        if(!blocks.isEmpty()) clear();
        FileWriter fileWriter=null;
        try{
            fileWriter=new FileWriter(file);
            fileWriter.write(blocks.size()+" "+blocks.getFirst().size()+" ");
            for(int i=0;i<blocks.size();i++)
                for(int j=0;j<blocks.get(i).size();j++){
                    Block block=blocks.get(i).get(j);
                    fileWriter.write(block.type.get()+" "+(block.value.get()?1:0)+" ");
                }
            for(int i=0;i<blocks.size();i++)
                for(int j=0;j<blocks.get(i).size();j++){
                    Block block=blocks.get(i).get(j);
                    fileWriter.write(block.input.size()+" "+block.output.size()+" ");
                    for(Block b:block.input) fileWriter.write(b.x+" "+b.y+" ");
                    for(Block b:block.output) fileWriter.write(b.x+" "+b.y+" ");
                }
        }
        catch(Exception e){
            e.printStackTrace();
            try{
                fileWriter.close();
                fileWriter=new FileWriter(file);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            }
            catch(Exception e2){e2.printStackTrace();}
        }
        finally{
            try{fileWriter.close();}
            catch(Exception e){e.printStackTrace();}
        }
        return true;
    }
}
