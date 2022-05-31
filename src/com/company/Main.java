package com.company;
import java.io.*;
import java.util.*;

class Block
{
    int base, limit;
    ArrayList<Integer> indexedBlocks;
    Block(int base, int limit){
        this.base=base;
        this.limit=limit;
    }
    Block(int x){
        indexedBlocks.add(x);
    }
}
class myFile
{
    Block location = new Block(-1,-1);
    public String name;
    public String filePath;
    public int[] allocatedBlocks;  // contigous
    public boolean deleted = false;
    public ArrayList<Block> linkedBlocks;
    public int indexedBlock; //
    public Block index_block;
    public myFile() {
        super();
    }

    public myFile(String name, String filePath, ArrayList<Block> linkedBlocks,int[] allocatedBlocks, boolean deleted) {
        this.name = name;
        this.filePath = filePath;
        this.linkedBlocks=new ArrayList<Block>();
        this.allocatedBlocks = allocatedBlocks;
        this.deleted = deleted;
    }


    public myFile(String path) {
        filePath=path;
        deleted=false;
        String [] list=path.split("/");
        name=list[list.length-1];
    }

}
class Directory {
    public String name;
    public String directoryPath;
    public ArrayList<myFile> myFiles;
    public ArrayList<Directory> subDirectories;
    public Map<String,String> map = new HashMap<String,String>();
    public boolean deleted = false;

    public Directory() {
        super();
    }
    public Directory(String directoryPath) {
        this.directoryPath = directoryPath;
        String [] list=directoryPath.split("/");
        name=list[list.length-1];
        myFiles=new ArrayList<>();
        subDirectories=new ArrayList<>();
        deleted=false;
    }
    public String getAccess(String id)
    {
        if(id.equals("admin"))
            return "11";
        if(map.containsKey(id))
            return map.get(id);
        else
            return "00";
    }
    public String store(String x,int choice,Technique obj)
    {
        for(int i=0;i<myFiles.size();i++)
        {
            x += myFiles.get(i).filePath;
            x += " ";
            if (choice == 1) //  contigous
            {
                x+=myFiles.get(i).location.base;
                x+=" ";
                x+= myFiles.get(i).location.base+myFiles.get(i).location.limit-1;
            }
            if( choice==2) {

                int j;
                int counter=0;

                for (j = 0; j < myFiles.get(i).linkedBlocks.size() - 1; j++)
                {
                    counter++;
                    x += myFiles.get(i).linkedBlocks.get(j).base + ",";
                    x += myFiles.get(i).linkedBlocks.get(j).limit + "\n";
                }

                x += myFiles.get(i).linkedBlocks.get(counter).base + ",";
                x += myFiles.get(i).linkedBlocks.get(counter).limit + "\n";
            }

            else if(choice==3)
            {
                x+=myFiles.get(i).indexedBlock+"\n";
                x+=myFiles.get(i).indexedBlock+"    ";
                int k=0;
                for(k=0;k<myFiles.get(i).index_block.indexedBlocks.size(); k++)
                    x+=myFiles.get(i).index_block.indexedBlocks.get(k)+" ";
            }
            x+="\n";
        }
        for(int i=0;i<subDirectories.size();i++)
        {
            x+=subDirectories.get(i).directoryPath;
            x+="\n";
        }
        for(int i=0;i<subDirectories.size();i++)
            x+=subDirectories.get(i).store("",choice,obj);
        return x;
    }
    public void printDirectoryStructure(String tab)
    {
        for(int i = 0; i< myFiles.size() ; i++ )
        {
            System.out.println(tab+ myFiles.get(i).name);
        }
        for( int i=0 ; i<subDirectories.size() ; i++ )
        {
            String Dir[] = subDirectories.get(i).directoryPath.split("/");
            System.out.println(tab+ "<" + Dir[ Dir.length - 1 ] + ">" );
            subDirectories.get(i).printDirectoryStructure(tab+"\t");
        }
    }
    public String fill(String x) {
        for(int i=0;i<subDirectories.size();i++)
        {
            Directory dir= subDirectories.get(i);
            x+=dir.directoryPath+",";
            //iteration
            for(Map.Entry<String,String>entry:dir.map.entrySet()){
                x+=entry.getKey()+","+entry.getValue();
            }
            x+="\n";
            dir.fill(x);
        }
        return x;
    }
}

class Capability {
    public Directory folder;
    public int create;
    public int delete;
}
class User {
    private String username;
    private String password;
    public ArrayList<Capability>capabilities;

    User(String username,String password , int[] capabilities){
        this.username = username;
        this.password = password;
        this.capabilities=new ArrayList<>();
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}

abstract class Technique{

    public static int[] disk;
    public Directory root;
    public ArrayList<ArrayList<Integer>> Indices = new ArrayList<ArrayList<Integer>>();

    public abstract boolean CreateFile(myFile myFile, int fileSize);
    public abstract void LoadFile(myFile file, int[] arr);
    public abstract void DeleteFile(myFile myFile);

    public Directory checkPath(String path)
    {
        String []list=path.split("/");
        Directory cur=root;
        if (!list[0].equals("root"))
            return null;

        for (int i=1; i< list.length-1; i++) //-2 (root and file)
        {
            if(cur.subDirectories.size()==0)
            {
                return null;
            }
            for (int k=0; k<cur.subDirectories.size(); k++)
            {
                if(list[i].equals(cur.subDirectories.get(k).name))
                {
                    cur=cur.subDirectories.get(k);
                    break;
                }
            }
        }
        if(list[list.length-2].equals(cur.name))
            return cur;
        return null;


    }
    public boolean CreateFolder(String path)
    {
        String []list=path.split("/");
        if(checkPath(path)!=null)
        {
            Directory cur = checkPath(path);
            for (int i=0; i<cur.subDirectories.size(); i++)
            {
                /*
                if(path.equals(cur.subDirectories.get(i).directoryPath))
                    return false;

                 */
                if(list[list.length-1].equals(cur.subDirectories.get(i).name)) {
                    return false;
                }
            }
            cur.subDirectories.add( new Directory(path) );
            return true;
        }
        return false;
    }
    public void DeleteFolder(Directory curr)
    {
        for (int i = 0; i<curr.myFiles.size(); i++)
        {
            this.DeleteFile(curr.myFiles.get(i));
        }
        for (int j=0; j<curr.subDirectories.size(); j++)
        {
            DeleteFolder(curr.subDirectories.get(j));
        }
        curr.myFiles.clear();
        curr.subDirectories.clear();
    }
    public void DisplayDiskStatus()
    {
        int empty = 0, allocated = 0;
        for (int i=0; i< disk.length; i++)
        {
            System.out.println("Disk of "+ i +" = [ "+ disk[i]+" ]");
            if(disk[i]==0)
                empty++;
            else
                allocated++;
        }
        System.out.println("empty blocks in the disk = "+empty);
        System.out.println("allocated blocks in the disk = "+allocated);

    }
}

class Linked_Allocation extends Technique
{
    int getSpace(int x)
    {
        while (disk[x]!=0)
            x++;
        return x;
    }
    @Override
    public boolean CreateFile(myFile myFile, int fileSize) {

        int count=0;
        boolean check =false;
        int size = fileSize; //20
        Block b = new Block(-1,-1);
        myFile.linkedBlocks=new ArrayList<Block>();
        for (int i = 0; i<disk.length; i++)
        {
            if(size > 1){ //2

                if(disk[i]==0) {
                    size--; //1
                    disk[i]=1;
                    if(b.base ==-1)  //base
                    {
                        b.base =i; //0}
                    }
                    else if(b.base!=-1 && b.limit ==-1) {
                        b.limit = i;
                        myFile.linkedBlocks.add(b); // {[12,15]}
                        b = new Block(b.limit,-1);
                    }
                    if(size==1&&disk[getSpace(i+1)]==0) {
                        b.limit=getSpace(i+1);
                        myFile.linkedBlocks.add(b);
                        disk[getSpace(i+1)]=1;
                        b = new Block(getSpace(i)-1,-1);
                        myFile.linkedBlocks.add(b); //{[12,15], [15,null]}
                        check = true;
                        break;
                    }
                }
            }
        }
        for (int i=0;i<myFile.linkedBlocks.size();i++)
        {
            System.out.println("base: "+myFile.linkedBlocks.get(i).base+" next: "+myFile.linkedBlocks.get(i).limit);
        }
        if(check==false){
            DeleteFile(myFile);
        }
        return check;
    }
    @Override
    public void LoadFile(myFile file, int[] arr) {
        file.allocatedBlocks=arr;
        for (int i=0;i<arr.length;i++)
            disk[arr[i]]=1;
    }
    @Override
    public void DeleteFile(myFile myFile)
    {
        for (int i = 0; i< myFile.linkedBlocks.size(); i++)
        {
            disk[myFile.linkedBlocks.get(i).base] = 0;
        }
        myFile.deleted = true;
    }
}
class Indexed_Allocation extends Technique
{
    public int getRandomBlock(int min, int max) {
        int index = -1;
        while(true) {
            index = (int) ((Math.random() * (max - min)) + min);
            if (disk[index] == 0)
            {
                disk[index]=1;
                break;
            }
        }
        return index;
    }
    @Override
    public boolean CreateFile(myFile myFile, int fileSize)
    {
        boolean check = false;
        int size = fileSize;
        int index_block = getRandomBlock(0, disk.length);
        if(index_block==-1)
            return false;

        myFile.indexedBlock = index_block;
        Block b = new Block(index_block, -1);
        b.indexedBlocks=new ArrayList<Integer>();
        myFile.index_block = b;
        for (int i=0; i< disk.length ; i++)
        {
            if (size>0)
            {
                if (disk[i]==0)
                {
                    disk[i]=1;
                    myFile.index_block.indexedBlocks.add(i);
                    size--;
                    check = true;
                }
            }
        }
        System.out.println("The index block is: "+index_block);
        System.out.print("The content of index block is: ");
        for(int i=0;i<myFile.index_block.indexedBlocks.size();i++){
            System.out.print(myFile.index_block.indexedBlocks.get(i)+" ");
        }
        System.out.println();
        if(check==false){
            DeleteFile(myFile);
        }
        return check;
    }
    @Override
    public void LoadFile(myFile file, int[] arr) {

        for (int i=1;i<arr.length;i++)
        {
            disk[arr[i]]=1 ;
        }
    }
    @Override
    public void DeleteFile(myFile myFile)
    {
        for (int i = 0; i< myFile.index_block.indexedBlocks.size(); i++)
        {
            disk[myFile.index_block.indexedBlocks.get(i)] = 0;

        }
        disk[myFile.indexedBlock]=0;
        myFile.deleted = true;
    }
}
class Contigous_Allocation extends Technique
{
    public Block getMin (ArrayList<Block> b)
    {
        int min = 123456789;
        int index = 0;
        for (int i=0; i<b.size(); i++)
        {
            if(b.get(i).limit<min){
                min = b.get(i).limit;
                index = i;
            }
        }
        return b.get(index);
    }
    // {[1,3], [10,2], [15,5]}
    @Override
    public boolean CreateFile(myFile myFile, int fileSize)
    {
        ArrayList<Block> freeBlocks = new ArrayList<>(); // 0 1 1 1 1 0 1 0 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0
        int count=0;                                    // ,[4,1],[8,20]
        int index=0;
        int j=0;
        for (int i=0; i< disk.length; i++)
        {
            if(disk[i]==0){
                count++;
            }else{
                if(count>0) {
                    Block b;
                    b = new Block(i-count,count);
                    freeBlocks.add(b);
                    count = 0;
                }
            }
        }
        if(count>0) {
            Block b;
            b = new Block(disk.length-count,count);
            freeBlocks.add(b);
        }
        Block b2 = new Block(-1,-1) ;
        ArrayList<Block> fBlocks = new ArrayList<>();
        fBlocks = freeBlocks;
        boolean check = false;
        while (true)
        {
            if(fBlocks.size()==0)
                break;
            b2 = getMin(fBlocks);
            if(b2.limit >= fileSize) // 1> 20
            {
                myFile.location=new Block(b2.base,fileSize);
                myFile.allocatedBlocks=new int[1000];
                for (int i=b2.base; i< fileSize+ b2.base; i++){
                    disk[i] = 1;
                    myFile.allocatedBlocks[i] =1;
                }
                check = true;
                break;
            }
            else{
                fBlocks.remove(b2);
            }
        }
        System.out.println("start = "+b2.base+" ,end = " +(int)(b2.base+fileSize-1));
        return check;
    }
    @Override
    public void LoadFile(myFile file, int[] arr) {
        file.allocatedBlocks = arr;
        for(int i=arr[0];i<arr[0]+arr[1] ;i++){
            disk[i] = 1;
        }
    }
    @Override
    public void DeleteFile(myFile myFile)
    {
        int size = myFile.allocatedBlocks.length; //10
        for (int i = myFile.location.base; i<size; i++)
        {
            if(i<disk.length && myFile.allocatedBlocks[i]==1)
            {
                disk[i]= 0;
                myFile.allocatedBlocks[i] = 0;
            }
        }
        myFile.deleted = true;
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
        int currentID=0;
        Scanner scan = new Scanner(System.in);
        boolean load = false;
        ArrayList<User> users = new ArrayList<>();
        File obj = new File("vfs.txt");
        File fileForReadingUsers = new File("user.txt");
        Scanner scanUsers=new Scanner(fileForReadingUsers);
        Scanner myReader = new Scanner(obj);
        System.out.println("Enter 1 to load vfs\nEnter 2 to input");
        int choice1 = scan.nextInt();
        if (choice1 == 1)
            load = true;
        int choice2 = 0;
        int noOfBlocks = 100; //initially
        if (load == false) {
            int [] adminCap = {1,1};
            User intialUser = new User("admin","admin",adminCap);
            users.add(intialUser);
            currentID = 0;
            System.out.println("Please Enter The number of blocks");
            noOfBlocks = scan.nextInt();
            System.out.println("Please enter the allocation technique");
            System.out.println("1- Contingous Allocation");
            System.out.println("2- linked Allocation");
            System.out.println("3- Indexed Allocation");
            choice2 = scan.nextInt();
        }
        else {
            if (myReader.hasNextLine())
                noOfBlocks = Integer.parseInt(myReader.nextLine());
            if (myReader.hasNextLine())
                choice2 = Integer.parseInt(myReader.nextLine());
        }
        Technique technique = null;
        scan.nextLine();
        if(choice2==1){
            technique = new Contigous_Allocation();
        }
        else if(choice2==2){
            technique = new Linked_Allocation();
        }
        else{
            technique = new Indexed_Allocation();
        }
        technique.disk = new int[noOfBlocks];
        technique.root = new Directory("root");
        if (load == true)          //load vfs
        {

            int counter=0;
            String line;
            while (myReader.hasNextLine()) {
                line = myReader.nextLine();
                if(!line.contains(" ")&&!line.contains(","))
                {
                    technique.CreateFolder(line);
                    continue;
                }
                if (choice2==1)
                {
                    String[] parts = line.split(" ");
                    myFile file=new myFile();
                    file.name=parts[0];

                    int base=Integer.parseInt(parts[1]);
                    int end=Integer.parseInt(parts[2]);
                    technique.CreateFile(file,end-base+1);
                }
                else if (choice2==2)
                {
                    if (line.length()==0)
                        continue;
                    String[] parts = line.split(" ");
                    myFile file=new myFile();
                    file.name=parts[0];
                    int fileSize=1;

                    while (!line.contains("-"))
                    {
                        line = myReader.nextLine();
                        fileSize++;
                    }


                    technique.CreateFile(file,fileSize);

                }
                else if(choice2==3)
                {
                    if (line.length()==0)
                        continue;
                    String[] parts = line.split(" ");

                    System.out.println(parts.length);
                    myFile file=new myFile();
                    file.name=parts[0];
                    file.indexedBlock=Integer.parseInt(parts[1]);
                    Block b =new Block(Integer.parseInt(parts[1]),-1);
                    line=myReader.nextLine();

                    String[] parts2 = line.split(" ");
                    b.indexedBlocks=new ArrayList<>();


                    for(int i=4;i<parts2.length;i++)
                    {
                        b.indexedBlocks.add(Integer.parseInt(parts2[i]));
                    }
                    System.out.println();
                    file.index_block=b;
                    technique.CreateFile(file,parts2.length-1);
                }
            }
            obj = new File("user.txt");
            myReader = new Scanner(obj);
            while (myReader.hasNextLine())
            {
                line = myReader.nextLine();
                int[] userCap ={0,0};
                User u = new User(line.split(",")[0],line.split(",")[1],userCap);
                users.add(u);
            }

            obj = new File("capabilities.txt");
            myReader = new Scanner(obj);
            while (myReader.hasNextLine())
            {
                line = myReader.nextLine();

                String dataFromCapabilitiesFile []=line.split(",");
                for(int i=1;i<dataFromCapabilitiesFile.length;i=i+2)
                {
                    for(int userIndex=0;userIndex<users.size();userIndex++)
                    {
                        if (users.get(userIndex).getUsername().equals(dataFromCapabilitiesFile[i]))
                        {
                            for(int directoryIndex=0;directoryIndex<technique.root.subDirectories.size();directoryIndex++ )
                            {
                                if (technique.root.subDirectories.get(directoryIndex).name.
                                        equals(dataFromCapabilitiesFile[0]
                                                .substring(dataFromCapabilitiesFile[0].indexOf('/')+1))
                                )
                                {

                                    Capability capability=new Capability();
                                    capability.folder=technique.root.subDirectories.get(directoryIndex);
                                    capability.create=dataFromCapabilitiesFile[i+1].charAt(0)-'0';
                                    capability.delete=dataFromCapabilitiesFile[i+1].charAt(1)-'1';
                                    technique.root.subDirectories.get(directoryIndex).map.put(users.get(userIndex).getUsername(),dataFromCapabilitiesFile[i+1]);
                                    users.get(userIndex).capabilities.add(capability);
                                }
                            }
                        }
                    }
                }


            }
            System.out.println("finish Reading ");

            myReader.close();
        }
        String cmd = "hello";
        System.out.println("Enter command or load or  exit");
        cmd = scan.nextLine();
        while (!cmd.equals("exit")) {
            if (cmd.split(" ")[0].equals("CreateFile")) {
                String path = cmd.split(" ")[1];

                if (technique.checkPath(path) != null) {
                    Directory cur = technique.checkPath(path);
                    boolean flag = false;
                    if(cur.getAccess(users.get(currentID).getUsername()).charAt(0)=='1'){



                        for (int i = 0; i < cur.myFiles.size(); i++) {
                            if (cur.myFiles.get(i).filePath.equals(path))
                                flag = true;
                        }
                        if (flag)
                            System.out.println("the file already exist");
                        else {
                            int FileSize = Integer.parseInt(cmd.split(" ")[2]);
                            myFile file = new myFile(path);
                            if (technique.CreateFile(file, FileSize)) {
                                technique.checkPath(path).myFiles.add(file);
                                System.out.println("create file successfully");

                            } else
                                System.out.println("No Space !!!");
                        }
                    }
                    else{
                        System.out.println("You are not allowed to create file in this folder");
                    }
                } else System.out.println("path not exist");
            } else if (cmd.split(" ")[0].equals("DeleteFile")) {
                String path = cmd.split(" ")[1];
                if (technique.checkPath(path) != null) {
                    Directory cur = technique.checkPath(path);
                    boolean flag = false;
                    if(cur.getAccess(users.get(currentID).getUsername()).charAt(1)=='1'){

                        for (int i = 0; i < cur.myFiles.size(); i++) {
                            if (path.equals(cur.myFiles.get(i).filePath)) {
                                myFile file = cur.myFiles.get(i);
                                technique.DeleteFile(file);
                                cur.myFiles.remove(i);
                                System.out.println("deleted Successfully");
                                flag = true;
                                break;
                            }
                        }
                        if (flag == false)
                            System.out.println("file not exist !!!");
                    }
                    else{
                        System.out.println("You are not allowed to delete file in this folder");
                    }
                } else System.out.println("path not exist");
            } else if (cmd.split(" ")[0].equals("CreateFolder")) {
                String path = cmd.split(" ")[1];
                Directory cur = technique.checkPath(path);
                if(cur.getAccess(users.get(currentID).getUsername()).charAt(0)=='1'){

                    if (technique.CreateFolder(path))
                        System.out.println("create folder Successfully");
                    else
                        System.out.println("path not exist or folder already exist");
                }
                else
                    System.out.println("You are not allowed to create folder in this folder");

            } else if (cmd.split(" ")[0].equals("DeleteFolder")) {
                String path = cmd.split(" ")[1];
                boolean flag = false;
                if (technique.checkPath(path) != null) {
                    Directory cur = technique.checkPath(path);
                    if(cur.getAccess(users.get(currentID).getUsername()).charAt(1)=='1'){
                        for (int i = 0; i < cur.subDirectories.size(); i++) {
                            if (path.equals(cur.subDirectories.get(i).directoryPath)) {
                                technique.DeleteFolder(cur.subDirectories.get(i));
                                cur.subDirectories.remove(i);
                                System.out.println("delete folder Successfully");
                                flag = true;
                            }
                        }

                        if (flag == false)
                            System.out.println("path not exist");
                    }
                }else
                    System.out.println("You are not allowed to delete in this folder");
            } else if (cmd.split(" ")[0].equals("DisplayDiskStatus")) {
                technique.DisplayDiskStatus();
            } else if (cmd.split(" ")[0].equals("DisplayDiskStructure")) {
                System.out.println("<root>");
                technique.root.printDirectoryStructure("\t");
            }else if (cmd.split(" ")[0].equals("TellUser")) {
                System.out.println("the logged user :");
                System.out.println(users.get(currentID).getUsername());
            }else if (cmd.split(" ")[0].equals("CUser")) {

                if(currentID ==0)
                {
                    boolean f = false;
                    for (int i=0;i<users.size();i++)
                    {
                        if(users.get(i).getUsername().equals(cmd.split(" ")[1]))
                        {
                            f=true;
                            break;
                        }
                    }
                    if(!f)
                    {
                        int [] userCap = {0,0};
                        User u = new User(cmd.split(" ")[1],cmd.split(" ")[2],userCap);
                        users.add(u);
                        System.out.println("the user created successfully");
                    }else
                    {
                        System.out.println("this username is exist");
                    }

                }else
                {
                    System.out.println("You are not admin !!");
                }
            }else if (cmd.split(" ")[0].equals("Grant")) {
                String[]list =  cmd.split(" ");
                if(currentID == 0)
                {
                    Integer userId =0;
                    boolean f = false;
                    for (int i=0;i<users.size();i++)
                    {
                        if(users.get(i).getUsername().equals(list[1]))
                        {
                            f=true;
                            userId =i;
                            break;
                        }
                    }
                    if(f)
                    {
                        list[2] = list[2]+"/file.txt";
                        if(technique.checkPath(list[2])!= null)
                        {
                            Directory dir =technique.checkPath(list[2]);
                            dir.map.put(users.get(userId).getUsername(), list[3]);

                        }else{
                            System.out.println("this path not valid ");
                        }
                    }else
                    {
                        System.out.println("this user not exist ");
                    }
                }else{
                    System.out.println("U are not authorized");
                }
            }else if (cmd.split(" ")[0].equals("Login")) {
                boolean f =false;
                for(int i=0;i<users.size();i++){
                    if(users.get(i).getUsername().equals(cmd.split(" ")[1]) &&
                            users.get(i).getPassword().equals(cmd.split(" ")[2])){
                        currentID = i;
                        System.out.println("You logged in !");
                        f =true;
                        break;
                    }
                }
                if(!f)
                {
                    System.out.println("username or password are not valid");
                }
            }
            else if (cmd.equals("load")) {
                FileWriter writer = new FileWriter("vfs.txt");
                writer.write(noOfBlocks + "\n" + choice2 + "\n");
                writer.write(technique.root.store("",choice2,technique));
                writer.close();
                writer = new FileWriter("user.txt");
                for (int i =0;i<users.size();i++)
                {
                    writer.write( users.get(i).getUsername()+ "," +users.get(i).getPassword() + "\n");
                }
                writer.close();
                writer = new FileWriter("capabilities.txt");
                writer.write(technique.root.fill(""));
                writer.close();

            } else System.out.println("Error command");
            System.out.println("Enter command or load or exit");
            cmd = scan.nextLine();
        }
    }
}

//CreateFile root/file.txt 10
//CreateFolder root/folder2
//CreateFile root/folder2/file1.txt 10
//DisplayDiskStructure
//DisplayDiskStatus
//DeleteFile root/file.txt
//DeleteFolder root/folder1
//  TellUser
//  CUser bassant pass123
//  Grant bassant root/folder1 10
//  Login bassant pass123
// CreateFolder root/folder3
// CUser mina 1234