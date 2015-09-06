/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.lancedh.rifts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author LanceDH
 */
public class Maze {
    private final int GROUND_Y = 1;
    private final int MAZE_GROUND = 32;
    
    private Server _server;
    private World _world;
    
    private ChunkStorage _storage;
    private Location _startPoint;
    private List<Location> _mobSpawns;
    private Location _bossSpawn;
    private Chunk[][] _maze;
    
    private int _level = 0;
    private int _chunkSize = 0;
    private int _nextX = 0 ;
    private int _nextY = 0 ;
    private int _prevDir = 0;
    private int _mazeSize = 0;

    public Maze(World w) {
        _world = w;
        _storage = new ChunkStorage();
        _mobSpawns = new ArrayList<Location>(); 
        
        
        InitChunkSize();
        FillChunkStorage();
    }
    
    //
    // Getters
    //
    
    public Location GetStartPoint(){
        return _startPoint;
    }
    
    public List<Location> GetMobSpawns(){
        return _mobSpawns;
    }
    
    public Location GetBossSpawn(){
        return _bossSpawn;
    }
    
    public int GetLevel(){
        return _level;
    }
    
    //
    // Everything else
    //
    
    private void InitChunkSize(){
        Location loc = new Location(_world, 0, GROUND_Y-1, -1);
        int count = 1;
        while (count <= 16 && loc.getBlock().getType() != Material.WOOL) {
            count += 1;
            loc.add(1, 0, 0);
        }
        _chunkSize = count;
    }
    
    public void CreateMaze(int size){
        
        _mazeSize = size; // (size / 2 ) * 2 + 1 ;
        _maze = new Chunk[_mazeSize][_mazeSize];
        _mobSpawns.clear();
        CreateStart();
        
        long start = System.nanoTime();

            WipeMaze();
            
            long end = System.nanoTime();
            System.out.println("Wipe took: " + (end - start)/1e6);
            end = start;
            start = System.nanoTime();
            
            // gen a maze for as long as possible
            while (GenNextChunk()) {}
            // Create end room at current end point
            GenEndRoom();
            // Start adding branches
            while(SetUpNextBranch()){
 
                // Create branch
                while (GenNextChunk()) {}
            }
            end = System.nanoTime();
            System.out.println("Generated in: " + (end - start)/1e6);
            end = start;
            start = System.nanoTime();
            
            DrawMaze();
             
            end = System.nanoTime();
            System.out.println("Drawn in: " + (end - start)/1e6);
            
            SetStartPoint();
            SetMobSpawns();
            
            _level++;
    }
    
    private void GenEndRoom(){
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
    
    private void WipeMaze(){
        for (int i = 0; i < _mazeSize; i++) {
            for (int j = 0; j < _mazeSize; j++) {
                _maze[i][j] = null;
            }
        }
        
        for (int x = -_mazeSize * _chunkSize / 2-1; x < _mazeSize * _chunkSize / 2+1; x++) {
            for (int z = -_mazeSize * _chunkSize / 2-1; z < _mazeSize * _chunkSize / 2+1; z++) {
                for (int y = MAZE_GROUND; y < MAZE_GROUND + _chunkSize; y++) {
                   Location nloc = new Location(_world, x, y, z);
                   if(IsPlacedInSecondRunthrough(nloc.getBlock().getState())){
                        nloc.getBlock().setType(Material.AIR);
                   }
               }
            }
        }
        
        for (int x = -_mazeSize * _chunkSize / 2-1; x < _mazeSize * _chunkSize / 2+1; x++) {
            for (int z = -_mazeSize * _chunkSize / 2-1; z < _mazeSize * _chunkSize / 2+1; z++) {
                for (int y = MAZE_GROUND; y < MAZE_GROUND + _chunkSize; y++) {
                   Location nloc = new Location(_world, x, y, z);
                   if(!IsPlacedInSecondRunthrough(nloc.getBlock().getState())){
                        nloc.getBlock().setType(Material.AIR);
                   }
               }
            }
        }
        
        CreateStart();
    }
    
    private void CreateStart(){
        int x = _mazeSize/2;
        int y = _mazeSize/2;
        _maze[x][y] = new Chunk(0, x, y);

        _nextX = x;
        _nextY = y - 1;
        _prevDir = 4;
        _maze[x][y - 1] = new Chunk(-1, _nextX, _nextY);
    }
    
    private boolean SetUpNextBranch(){
        
        Random rng = new Random();
        
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
        c = list.get(rng.nextInt(list.size()));

        // Only picks the first available, should pick random if more
        int[] freeSpots = c.getFreeSpots();
        for (int i = 0; i < 4; i++) {
            if(freeSpots[i] == longest){
                StartBranch(c, (int)Math.pow(2, i));
                break;
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
    
    private void StartBranch(Chunk c, int dir) {
        _nextX = c.GetxPos();
        _nextY = c.GetyPos();
        SetNextValues(_nextX, _nextY, dir);
        c.AddDirectionToChunkId(dir);
                
    }
    
    private boolean GenNextChunk(){
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
        Random rng = new Random();
        
        int dir = -1;
        if(!list.isEmpty()){
            dir = list.get(rng.nextInt(list.size()));
        }
        
        
        return dir;
    }
    
    private void DrawMaze(){
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
        BlockState[][][] chunk = _storage.GetChunkOfId(chunkId);
        BlockState bs = null;
        Location nloc = new Location(_world, xPos, GROUND_Y, yPos-_chunkSize*_mazeSize);

        for (int x = 0; x < _chunkSize; x++) {
            for (int y = 0; y < _chunkSize; y++) {
                for (int z = 0; z < _chunkSize; z++) {
                    bs =  chunk[x][y][z];
                    if(!IsPlacedInSecondRunthrough(bs)){
                    //start point of chunk
                    nloc = new Location(_world, xPos, GROUND_Y, yPos-_chunkSize*_mazeSize);
                    //move location per block
                    nloc.add(x, y, z);
                    
                    
                    nloc.getBlock().setType(bs.getType());
                    nloc.getBlock().setData(bs.getData().getData());
                    
                    SetSpecificData(bs, nloc);
                }
                }
            }
        }
        
        //torches
        for (int x = 0; x < _chunkSize; x++) {
            for (int y = 0; y < _chunkSize; y++) {
                for (int z = 0; z < _chunkSize; z++) {
                    bs =  chunk[x][y][z];
                    if(IsPlacedInSecondRunthrough(bs)){
                    //start point of chunk
                    nloc = new Location(_world, xPos, GROUND_Y, yPos-_chunkSize*_mazeSize);
                    //move location per block
                    nloc.add(x, y, z);

                    nloc.getBlock().setTypeIdAndData(bs.getTypeId(), bs.getData().getData(), false);
                    }
                }
            }
        }
    }
    
    private void SetSpecificData(BlockState bs, Location loc){
        // Signs
        if(bs instanceof Sign){
            Sign state = (Sign) bs;
            Sign target = (Sign) loc.getBlock().getState();
            for (int i = 0; i < 4; i++) {
                target.setLine(i, state.getLine(i));
                target.update();
            }
        }
        
        // Banners
        if(bs instanceof Banner){
            Banner state = (Banner) bs;
            Banner target = (Banner) loc.getBlock().getState();
            target.setBaseColor(state.getBaseColor());
            target.setPatterns(state.getPatterns());
            target.update();
        }
    }
    
    private void SetStartPoint(){
        Location nloc = null;
        Sign sign = null;
        for (int x = -_mazeSize * _chunkSize / 2-1; x < _mazeSize * _chunkSize / 2+1; x++) {
            for (int z = -_mazeSize * _chunkSize / 2-1; z < _mazeSize * _chunkSize / 2+1; z++) {
                for (int y = GROUND_Y; y < GROUND_Y + _chunkSize; y++) {
                    nloc = new Location(_world, x, y, z);
                    nloc = nloc.add(0, 0, -_mazeSize * _chunkSize);
                    if(nloc.getBlock().getState() instanceof Sign){
                        sign = (Sign) nloc.getBlock().getState();
                        if("[start]".equals(sign.getLine(0).toLowerCase())){
                            _startPoint = nloc;
                            _startPoint = _startPoint.add(0.5, 0, 0.5);
                            _startPoint.setDirection(new Vector(nloc.getX(), nloc.getY(), nloc.getZ() - 1));
                            
                            nloc.getBlock().setType(Material.AIR);
                            return;
                        }
                    }
                }
            }
        }
        System.out.println("No start fround");
    }
    
    private void SetMobSpawns(){
        Location nloc = null;
        Skull skull = null;
        Sign sign = null;
        for (int x = -_mazeSize * _chunkSize / 2-1; x < _mazeSize * _chunkSize / 2+1; x++) {
            for (int z = -_mazeSize * _chunkSize / 2-1; z < _mazeSize * _chunkSize / 2+1; z++) {
                for (int y = GROUND_Y; y < GROUND_Y + _chunkSize; y++) {
                    nloc = new Location(_world, x, y, z);
                    nloc = nloc.add(0, 0, -_mazeSize * _chunkSize);
                    if(nloc.getBlock().getState() instanceof Skull){
                        _mobSpawns.add(new Location(nloc.getWorld(), nloc.getBlockX()+0.5, nloc.getBlockY()+0.5, nloc.getBlockZ()+0.5));
                        nloc.getBlock().setType(Material.AIR);
                    }
                    // would use skulls but shit's broken
                    if(nloc.getBlock().getState() instanceof Sign){
                        sign = (Sign) nloc.getBlock().getState();
                        if("[boss]".equals(sign.getLine(0).toLowerCase()) ){
                            _bossSpawn = new Location(nloc.getWorld(), nloc.getBlockX()+0.5, nloc.getBlockY()+0.5, nloc.getBlockZ()+0.5);
                            nloc.getBlock().setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
    
    private boolean IsPlacedInSecondRunthrough(BlockState bs){
        if(bs.getType() == Material.TORCH){
            return true;
        }
        
        if(bs.getType() == Material.LADDER){
            return true;
        }
        
        if(bs.getType() == Material.TRIPWIRE_HOOK){
            return true;
        }
        
        if(bs.getType() == Material.VINE){
            return true;
        }
        
        if(bs.getType() == Material.LONG_GRASS){
            return true;
        }
        
        if(bs.getType() == Material.REDSTONE_LAMP_ON){
            return true;
        }
        
        if(bs.getType() == Material.GLOWSTONE){
            return true;
        }
        
        if(bs.getType() == Material.SEA_LANTERN){
            return true;
        }
        
        return false;
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
    
    public void FillChunkStorage(){
        _storage.Reset();
        Location nloc = null;
        int variant = 0;
        for (int i = 0; i <= 19; i++) {
            variant = 0;
            do{
                _storage.AddChunk(i, CopyChunk(i, variant));
                variant += _chunkSize;
                nloc = new Location(_world, variant, GROUND_Y, i*_chunkSize);
            }while(nloc.getBlock().getType() != Material.AIR);
        }
    }
    
    private BlockState[][][] CopyChunk(int id, int variant){
        BlockState[][][] chunk = new BlockState[_chunkSize][_chunkSize][_chunkSize];
        Location nloc = null;
        do{
            for (int x = 0; x < _chunkSize; x++) {
                for (int y = 0; y < _chunkSize; y++) {
                    for (int z = 0; z < _chunkSize; z++) {
                        //start point of chunk
                        nloc = new Location(_world, variant, GROUND_Y, id*_chunkSize);
                        //move location per block
                        nloc.add(x, y, z);
                        chunk[x][y][z] = nloc.getBlock().getState();
                    }
                }
            }
        }while(nloc == null);
        
        return chunk;
    }


    private void DEBUGDrawMaze(){
        String s = "";
        for (int i = 0; i < _mazeSize; i++) {
            for (int j = 0; j < _mazeSize; j++) {
                s += (char)DEBUGGetChunkChar(_maze[j][i]);
            }
            System.out.println(s);
            s = "";
        }
        System.out.println(" ");
    }
    
    private int DEBUGGetChunkChar(Chunk c){
        if(c == null){ return 176;}
        switch(c.GetChunkId()){
            case 0:
                return 2;
            case 1:
                return 32;
            case 2:
                return 16;
            case 3:
                return 192;
            case 4:
                return 31;
            case 5:
                return 179;
            case 6:
                return 218;
            case 7:
                return 195;
            case 8:
                return 17;
            case 9:
                return 217;
            case 10:
                return 196;
            case 11:
                return 193;
            case 12:
                return 191;
            case 13:
                return 180;
            case 14:
                return 194;
            case 15:
                return 197;
            case 16:
                return 219;
            case 17:
                return 219;
            case 18:
                return 219;
            case 19:
                return 219;
        }
        return 63;
    }
}
