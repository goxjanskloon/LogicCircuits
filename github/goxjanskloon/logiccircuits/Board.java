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
    public class Pos{
        public int x,y;
        public Pos(){x=y=-1;}
        public Pos(int initX,int initY){x=initX;y=initY;}
    }
    class Block{
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
        private AtomicInteger type;
        private AtomicBoolean value;
        private CopyOnWriteArrayList<Pos> inPos,outPos;
        public Block(int initType,boolean initValue){
            type=new AtomicInteger(initType);
            value=new AtomicBoolean(initValue);
        }
    }
    private class FlushBlock implements Runnable{
        Pos pos;
        public FlushBlock(Pos argPos){pos=argPos;}
        public void run(){
            Block block=getBlock(pos);
            boolean newValue;
            switch(Block.Type.valueOf(block.type.get())){
            case Void:newValue=!block.inPos.isEmpty()&&getBlock(block.inPos.getFirst()).value.get();break;
            case Or:newValue=block.inPos.size()>=2&&(getBlock(block.inPos.getFirst()).value.get()||getBlock(block.inPos.get(1)).value.get());break;
            case Not:newValue=!block.inPos.isEmpty()&&!getBlock(block.inPos.getFirst()).value.get();break;
            case And:newValue=block.inPos.size()>=2&&getBlock(block.inPos.getFirst()).value.get()&&getBlock(block.inPos.get(1)).value.get();break;
            case Xor:newValue=block.inPos.size()>=2&&(getBlock(block.inPos.getFirst()).value.get()^getBlock(block.inPos.get(1)).value.get());break;
            case Src:newValue=block.value.get();break;
            default:newValue=false;
            }
            if(newValue!=block.value.get()){
                block.value.set(newValue);
                for(Pos outPos:block.outPos) threadPool.execute(new FlushBlock(outPos));
            }
        }
    }
    private ArrayList<ArrayList<Block>> blocks;
    private ExecutorService threadPool=Executors.newCachedThreadPool(new ThreadFactory(){
        public Thread newThread(Runnable runnable){return new Thread(runnable);}
    });
    Block getBlock(Pos pos){
        return blocks.get(pos.x).get(pos.y);
    }
    public boolean isEmpty(){return blocks.isEmpty();}
    public int getWidth(){return blocks.size();}
    public int getHeight(){
        if(isEmpty()) return 0;
        return blocks.getFirst().size();
    }
    public Block.Type getBlockType(Pos pos){
        return Block.Type.valueOf(getBlock(pos).type.get());
    }
    public Block.Type getBlockType(Block block){
        return Block.Type.valueOf(block.type.get());
    }
    public boolean getBlockValue(Pos pos){
        return getBlock(pos).value.get();
    }
    public boolean getBlockValue(Block block){
        return block.value.get();
    }
    public void flushBlock(Pos pos){
        threadPool.execute(new FlushBlock(pos));
    }
    public boolean setBlockType(Pos pos,int type){
        Block block=getBlock(pos);
        if(type!=block.type.get()){
            block.type.set(type);
            flushBlock(pos);
            return true;
        }
        return false;
    }
    public boolean linkBlocks(Pos lPos,Pos rPos){
        Block lBlock=getBlock(lPos),rBlock=getBlock(rPos);
        for(Pos outPos:lBlock.outPos)
            if(outPos==rPos) return false;
        lBlock.outPos.add(rPos);
        rBlock.inPos.add(lPos);
        return true;
    }
    public boolean setSrcValue(Pos pos,boolean value){
        Block block=getBlock(pos);
        if(Block.Type.valueOf(block.type.get())!=Block.Type.Src||block.value.get()==value) return false;
        block.value.set(value);
        return true; 
    }
    public boolean clearBlock(Pos pos){
        Block block=getBlock(pos);
        if(block.type.get()==0)
            if(block.inPos.isEmpty())
                if(block.outPos.isEmpty()) return false;
                else{
                    for(Pos outPos:block.outPos){
                        Block outBlock=getBlock(outPos);
                        for(int i=0;i<outBlock.inPos.size();i++)
                            if(outBlock.inPos.get(i)==pos){
                                outBlock.inPos.remove(i);
                                break;
                            }
                    }
                    block.outPos.clear();
                }
            else{
                for(Pos inPos:block.inPos){
                    Block inBlock=getBlock(inPos);
                    for(int i=0;i<inBlock.outPos.size();i++)
                        if(inBlock.outPos.get(i)==pos){
                            inBlock.outPos.remove(i);
                            break;
                        }
                }
                block.inPos.clear();
            }
        else block.type.set(0);
        return true;
    }
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
                    blocks.getLast().add(new Block(scanner.nextInt(),scanner.nextInt()==1));
                    Block block=blocks.getLast().getLast();
                    for(int size=scanner.nextInt();size-->0;) block.inPos.add(new Pos(scanner.nextInt(),scanner.nextInt()));
                    for(int size=scanner.nextInt();size-->0;) block.outPos.add(new Pos(scanner.nextInt(),scanner.nextInt()));
                }
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
                    fileWriter.write(block.type.get()+" "+(block.value.get()?1:0)+" "+block.inPos.size()+" "+block.outPos.size()+" ");
                    for(Pos pos:block.inPos) fileWriter.write(pos.x+" "+pos.y+" ");
                    for(Pos pos:block.outPos) fileWriter.write(pos.x+" "+pos.y+" ");
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
