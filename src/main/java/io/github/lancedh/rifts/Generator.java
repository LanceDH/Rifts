/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getLogger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 *
 * @author LanceDH
 */
public class Generator {
    private final int GROUND_Y = 7;
    private final int MAZE_GROUND = 32;
    private int _chunkSize = 0;
    private int _nrSpawnedChunks = 0;
    private Chunk[][] _maze;
    private int _mazeSize = 0;
    private World _world;
    private int _nextX = 0 ;
    private int _nextY = 0 ;
    private int _prevDir = 0;
    private Random _rng;
    
    public Generator(Player p, int size) {
        //Always make maze an uneven number
        _mazeSize = (size / 2 ) * 2 + 1 ;
        _maze = new Chunk[_mazeSize][_mazeSize];
        _world = p.getWorld();
        _rng = new Random();
        CreateStart();
        InitChunkSize(p);
        //CopyChunk(p, 0);
    }
    
    private void CreateStart(){
        for (int x = -_mazeSize * _chunkSize / 2-1; x < _mazeSize * _chunkSize / 2+1; x++) {
            for (int z = -_mazeSize * _chunkSize / 2-1; z < _mazeSize * _chunkSize / 2+1; z++) {
                   Location nloc = new Location(_world, x, MAZE_GROUND, z);
                   nloc.getBlock().setType(Material.GRASS);
            }
        }
        
        int x = _mazeSize/2;
        int y = _mazeSize/2;
        _maze[x][y] = new Chunk(0, x, y);

        _nextX = x;
        _nextY = y - 1;
        _prevDir = 4;
        _maze[x][y - 1] = new Chunk(-1, _nextX, _nextY);
    }
    
    public void WipeMaze(){
        for (int i = 0; i < _mazeSize; i++) {
            for (int j = 0; j < _mazeSize; j++) {
                _maze[i][j] = null;
            }
        }
        
        for (int x = -_mazeSize * _chunkSize / 2-1; x < _mazeSize * _chunkSize / 2+1; x++) {
            for (int z = -_mazeSize * _chunkSize / 2-1; z < _mazeSize * _chunkSize / 2+1; z++) {
                for (int y = MAZE_GROUND; y < MAZE_GROUND + _chunkSize; y++) {
                   Location nloc = new Location(_world, x, y, z);
                   nloc.getBlock().setType(Material.AIR);
                }
            }
        }
        
        CreateStart();
    }
    
    public void DrawMaze(){
        int x = 0;
        int y = 0;
        for (int i = 0; i < _mazeSize; i++) {
            for (int j = 0; j < _mazeSize; j++) {
                //center spawn at 0,0
                x = - (_mazeSize * _chunkSize /2);
                y = - (_mazeSize * _chunkSize /2);
                        
                x += i * _chunkSize;
                y += j * _chunkSize;
                if(_maze[i][j] != null){
                    DrawChunk(_maze[i][j].GetChunkId(), x, y);
                }else{
                    //spawn a bedrock block to indicate empty chunks
                    Location loc = new Location(_world, x + _chunkSize/2, MAZE_GROUND, y + _chunkSize/2);
                    loc.getBlock().setType(Material.BEDROCK);
                }
            }
        }
    }
    
    private void DrawChunk(int chunkId, int xPos, int yPos){
        Block b = null;
        

        for (int x = 0; x < _chunkSize; x++) {
            for (int y = 0; y < _chunkSize; y++) {
                for (int z = 0; z < _chunkSize; z++) {
                    b =  _world.getBlockAt(x, GROUND_Y + y, chunkId * _chunkSize + z);
                    //start point of chunk
                    Location nloc = new Location(_world, xPos, MAZE_GROUND, yPos);
                    //move location per block
                   nloc.add(x, y, z);
                   nloc.getBlock().setType(b.getType());
                   nloc.getBlock().setData(b.getData());
                   // signs
                   if(b.getTypeId() == 0x3f){
                       Sign sOrigin = (Sign) b.getState();
                       Sign sNew = (Sign) nloc.getBlock().getState();
                       for (int i = 0; i < 4; i++) {
                           sNew.setLine(i, sOrigin.getLine(i));
                           sNew.update();
                       }
                   }
                }
            }
        }
        
        _nrSpawnedChunks += 1;
    }
    
    private void InitChunkSize(Player p){
        Location loc = new Location(p.getLocation().getWorld(), 0, GROUND_Y, -1);
        int count = 1;
        while (count <= 16 && loc.getBlock().getType() != Material.WOOL) {
            count += 1;
            loc.add(1, 0, 0);
        }
        _chunkSize = count;
    }
    
    private int CountEmptyChunksOnSide(int x, int y, int[][] array){
        int count = 0;
        if(array[x][y] != 0 ){
            //getLogger().info("hit something");
            return 0;
        }
        count += 1;
        array[x][y] = 1;
        if(x+1 <= _mazeSize - 1){
            count += CountEmptyChunksOnSide(x+1, y, array);
        }
        if(x-1 >= 0){
            count += CountEmptyChunksOnSide(x-1, y, array);
        }
        if(y+1 <= _mazeSize - 1){
            count += CountEmptyChunksOnSide(x, y+1, array);
        }
        if(y-1 >= 0){
            count += CountEmptyChunksOnSide(x, y-1, array);
        }
        
        return count;
    }
    
    private int GetOppositeDirection(int dir){
        switch(dir){
            case 1:
                return 4;
            case 2:
                return 8;
            case 4:
                return 1;
            case 8:
                return 2;
        }
        
        return -1;
    }
    
    private void CheckForPossibleBranches(){
        Chunk c = null;
        int[] freeSpots = new int[4];
        int[][] countArr = new int[_mazeSize][_mazeSize];
        
        for (int x = 0; x < _mazeSize; x++) {
            for (int y = 0; y < _mazeSize; y++) {
                if(_maze[x][y] != null && IsNotStartOrEnd(_maze[x][y])){
                    c = _maze[x][y];
                    
                    
                    for (int i = 0; i < 4; i++) {
                        freeSpots[i] = 0;
                    }
                    ResetCountArray(countArr);
                    if(y-1 >= 0){
                        freeSpots[0] = CountEmptyChunksOnSide(x, y-1, countArr);
                    }
                    ResetCountArray(countArr);
                    if(x+1 <= _mazeSize - 1){
                        freeSpots[1] = CountEmptyChunksOnSide(x+1, y, countArr);
                    }
                    ResetCountArray(countArr);
                    if(y+1 <= _mazeSize - 1){
                        freeSpots[2] = CountEmptyChunksOnSide(x, y+1, countArr);
                    }
                    ResetCountArray(countArr);
                    if(x-1 >= 0){
                        freeSpots[3] = CountEmptyChunksOnSide(x-1, y, countArr);
                    }

                    c.SetFreeSpots(freeSpots);
                }
            }
        }
            
    }
    
    public boolean SetUpNextBranch(){
        
        CheckForPossibleBranches();
        
        int longest = 0;
        // Find the longest possible branch
        for (int x = 0; x < _mazeSize; x++) {
            for (int y = 0; y < _mazeSize; y++) {
                if(_maze[x][y] != null && IsNotStartOrEnd(_maze[x][y]) && _maze[x][y].HasFreeSpots()){
                    for (int spots : _maze[x][y].getFreeSpots()) {
                        if (spots > longest) {
                            longest = spots;
                        }
                    }
                }
            }
        }
        
        
        
        if(longest == 0){
            // No branches left
            return false;
        }
        
        // Get all chunks with a direction to the longest branch
        ArrayList<Chunk> list = new ArrayList<>();
        Chunk c = null;
        for (int x = 0; x < _mazeSize; x++) {
            for (int y = 0; y < _mazeSize; y++) {
                if(_maze[x][y] != null && IsNotStartOrEnd(_maze[x][y]) && _maze[x][y].HasFreeSpots()){
                    c = _maze[x][y];
                    int[] freeSpots = c.getFreeSpots();
                    for (int i = 0; i < 4; i++) {
                        if(freeSpots[i] == longest){
                            list.add(c);
                        }
                    }
                }
            }
        }
      
        // Get a random winner
        c = list.get(_rng.nextInt(list.size()));

        // Only picks the first available, should pick random if more
        int[] freeSpots = c.getFreeSpots();
        for (int i = 0; i < 4; i++) {
            if(freeSpots[i] == longest){
                StartBranch(c, (int)Math.pow(2, i));
            }
        }
        
        return true;
    }
    
    private boolean IsNotStartOrEnd(Chunk c){
        if(c.GetChunkId() == 0 || // Start
                c.GetChunkId() == 16 || // End up
                c.GetChunkId() == 17 || // End right
                c.GetChunkId() == 18 || // end down
                c.GetChunkId() == 19 // End left
                ){
            return false;
        }
        return true;
    }
    
    public void GenEndRoom(){
        int id = 16;
        
        switch(_prevDir){
            case 2:
                id += 1;
                break;
            case 4:
                id += 2;
                break;
            case 8:
                id += 3;
                break;     
        }
        
        _maze[_nextX][_nextY] = new Chunk(id, _nextX, _nextY);
    }
    
    public boolean GenNextChunk(){
        int x = _nextX;
        int y = _nextY;
        int prevDir = _prevDir;
        int nextDir = GetNextDirection(_prevDir, x, y);
        if(nextDir == -1){
            // Maze encountered a dead end
            // Recreate dead end for 1 block branches
            _maze[_nextX][_nextY] = new Chunk(_prevDir, _nextX, _nextY);
            return false;
        }
        _maze[x][y] = new Chunk(prevDir + nextDir , x, y);
        SetNextValues(x, y, nextDir);
        // Create dead end for counting next path
        _maze[_nextX][_nextY] = new Chunk(_prevDir, _nextX, _nextY);
        
        return true;
    }
    
    private void SetNextValues(int x, int y, int dir){
        switch(dir){
            case 1:
                _nextY -= 1 ;
                _prevDir = 4;
                break;
            case 2:
                _nextX += 1 ;
                _prevDir = 8;
                break;
            case 4:
                _nextY += 1 ;
                _prevDir = 1;
                break;
            case 8:
                _nextX -= 1 ;
                _prevDir = 2;
                break;
        }
    }
    
    private int GetNextDirection(int prev, int x, int y){
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(4);
        list.add(8);
        
        // prevent going backwards
        list.remove(new Integer(prev));
        
        // Prevent going off the edges and overwriting
        // Left
        if(x == 0 || ( x > 0 &&  _maze[x-1][y] != null)){
            list.remove(new Integer(8));
        }
        // Right
        if(x == _mazeSize-1 || ( x < _mazeSize-1 &&  _maze[x+1][y] != null)){
            list.remove(new Integer(2));
        }
        // Top
        if(y == 0 || ( y > 0 && _maze[x][y-1] != null)){
            list.remove(new Integer(1));
        }
        // Bottom
        if(y == _mazeSize-1 || ( y < _mazeSize-1 &&  _maze[x][y+1] != null)){
            list.remove(new Integer(4));
        }
        
        
        // Counting which direction has the most free space
        int[][] countArr = new int[_mazeSize][_mazeSize];
        Map directionValues = new HashMap();
        int value = 0;
        for (int i = 0; i < list.size(); i++) {
            switch(list.get(i)){
                case 1:
                    ResetCountArray(countArr);
                    value = CountEmptyChunksOnSide(x, y-1, countArr);
                    directionValues.put(1, value);
                    break;
                case 2:
                    ResetCountArray(countArr);
                    value = CountEmptyChunksOnSide(x+1, y, countArr);
                    directionValues.put(2, value);
                    break;
                case 4:
                    ResetCountArray(countArr);
                    value = CountEmptyChunksOnSide(x, y+1, countArr);
                    directionValues.put(4, value);
                    break;
                case 8:
                    ResetCountArray(countArr);
                    value = CountEmptyChunksOnSide(x-1, y, countArr);
                    directionValues.put(8, value);
                    break;
            }
        }
        
        int highest = 0;
        for(Object key: directionValues.keySet()){
            if((int)directionValues.get(key) > highest){
                highest = (int)directionValues.get(key);
            }
            //getLogger().info(key + " - " + directionValues.get(key));
        }
        
        //getLogger().info("Highest: " + highest);
        
        // remove directions the have the least space
        for(Object key: directionValues.keySet()){
            if((int)directionValues.get(key) != highest){
                list.remove(new Integer((int)key));
                //getLogger().info("Removed " + key);
            }
            
        }
        
        // pick a random direction of the ones that are left
        int dir = -1;
        if(!list.isEmpty()){
            dir = list.get(_rng.nextInt(list.size()));
        }
        
        
        return dir;
    }
    
    // create a new integer
    private int[][] ResetCountArray(int[][] countArr){
        
        for (int i = 0; i < _mazeSize; i++) {
            for (int j = 0; j < _mazeSize; j++) {
                if(_maze[i][j] == null){
                    countArr[i][j] = 0;
                }else{
                    countArr[i][j] = 1;
                }
            }
        }
        return countArr;
    }
    
    public void CopyChunk(Player p, int chunkId){
        Location loc = p.getLocation();
        Block b = null;
        World w = loc.getWorld();
        
        for (int x = 0; x < _chunkSize; x++) {
            for (int y = 0; y < _chunkSize; y++) {
                for (int z = 0; z < _chunkSize; z++) {
                   b =  w.getBlockAt(x, GROUND_Y + y, chunkId * _chunkSize + z);
                   Location nloc = new Location(w, 0, MAZE_GROUND, _nrSpawnedChunks * _chunkSize);
                   nloc.add(x, y, z);
                   nloc.getBlock().setType(b.getType());
                   nloc.getBlock().setData(b.getData());
                   // signs
                   if(b.getTypeId() == 0x3f){
                       Sign sOrigin = (Sign) b.getState();
                       Sign sNew = (Sign) nloc.getBlock().getState();
                       for (int i = 0; i < 4; i++) {
                           sNew.setLine(i, sOrigin.getLine(i));
                           sNew.update();
                       }
                   }
                }
            }
        }
        
        _nrSpawnedChunks += 1;
    }

    private void StartBranch(Chunk c, int dir) {
        _nextX = c.GetxPos();
        _nextY = c.GetyPos();
        SetNextValues(_nextX, _nextY, dir);

        c.AddDirectionToChunkId(dir);
                
    }
}
