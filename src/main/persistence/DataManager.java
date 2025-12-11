package persistence;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Task;
import model.Wish;
import model.User;


import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public class DataManager {
    private final Gson gson = new Gson();
    private final File usersFile = new File("data/users.json");
    private final File tasksFile = new File("data/tasks.json");
    private final File wishesFile = new File("data/wishes.json");


    public DataManager(){
        try{ Files.createDirectories(usersFile.toPath().getParent());
            if(!usersFile.exists()) usersFile.createNewFile();
            if(!tasksFile.exists()) tasksFile.createNewFile();
            if(!wishesFile.exists()) wishesFile.createNewFile();
            initIfEmpty(usersFile);
            initIfEmpty(tasksFile);
            initIfEmpty(wishesFile);
        } catch(IOException e){ e.printStackTrace(); }
    }


    private void initIfEmpty(File f) throws IOException{
        if(f.length() == 0) try(Writer w = new FileWriter(f)){ w.write("[]"); }
    }


    private <T> List<T> readList(File f, Class<T> clazz){
        try(Reader r = new FileReader(f)){
            Type type = TypeToken.getParameterized(List.class, clazz).getType();
            List<T> list = gson.fromJson(r, type);
            return list == null ? new ArrayList<>() : list;
        }catch(Exception e){ e.printStackTrace(); return new ArrayList<>(); }
    }


    private <T> boolean writeList(File f, List<T> list){
        try(Writer w = new FileWriter(f)){ gson.toJson(list, w); return true; }
        catch(IOException e){ e.printStackTrace(); return false; }
    }


    public List<User> loadUsers(){ return readList(usersFile, User.class); }
    public boolean saveUsers(List<User> users){ return writeList(usersFile, users); }
    public List<Task> loadTasks(){ return readList(tasksFile, Task.class); }
    public boolean saveTasks(List<Task> tasks){ return writeList(tasksFile, tasks); }
    public List<Wish> loadWishes(){ return readList(wishesFile, Wish.class); }
    public boolean saveWishes(List<Wish> wishes){ return writeList(wishesFile, wishes); }
}