package org.goxjanskloon.lgct;
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
            Void(0),Or(1),Not(2),And(3),Xor(4),Src(5);
            private final int value;
            private Type(int argValue){value=argValue;}
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
            public int getValue(){return value;}
        }
        public AtomicInteger type;
        public AtomicBoolean value;
        public CopyOnWriteArrayList<Pos> inPos,outPos;
        public Block(){
            type=new AtomicInteger(0);
            value=new AtomicBoolean(false);
        }
        public Block(int initType,boolean initValue){
            type=new AtomicInteger(initType);
            value=new AtomicBoolean(initValue);
        }
    }
    class FlushThreadFactory implements ThreadFactory{
        AtomicInteger threadIdx=new AtomicInteger(0);
        String threadNamePrefix;
        public FlushThreadFactory(String Prefix){
            threadNamePrefix=Prefix;
        }
        public Thread newThread(Runnable runnable){
            Thread thread=new Thread(runnable);
            thread.setName(threadNamePrefix+"-xxljob-"+threadIdx.getAndIncrement());
            return thread;
        }
    }
    class FlushThread implements Runnable{
        Pos pos;
        public FlushThread(Pos argPos){pos=argPos;}
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
                for(Pos outPos:block.outPos) flushThreadPool.execute(new FlushThread(outPos));
            }
        }
    }
    ArrayList<ArrayList<Block>> blocks;
    ExecutorService flushThreadPool=Executors.newCachedThreadPool(new FlushThreadFactory("cachedThread"));
    Block getBlock(Pos pos){
        return blocks.get(pos.x).get(pos.y);
    }
    public boolean isEmpty(){return blocks.isEmpty();}
    public int getWidth(){return blocks.size();}
    public int getHeight(){
        if(isEmpty()) return 0;
        return blocks.get(0).size();
    }
    public Block.Type getBlockType(Pos pos){
        return Block.Type.valueOf(getBlock(pos).type.get());
    }
    public boolean getBlockValue(Pos pos){
        return getBlock(pos).value.get();
    }
    public void flushBlock(Pos pos){
        flushThreadPool.execute(new FlushThread(pos));
    }
    public boolean setType(Pos pos,int type){
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
    boolean setSrcValue(Pos pos,boolean value){
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
    void clear(){
        flushThreadPool.shutdownNow();
        blocks.clear();
    }
    boolean loadFile(File file){
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
    boolean exportFile(File file){
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
