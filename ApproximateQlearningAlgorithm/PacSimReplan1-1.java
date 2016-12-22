
/*
###############UCF Spring 2015###################
PID 3667008 2nd semester
Author: S.Santhosh Chaitanya
feature vectors:
	No of ghosts in 1 unit cell from the next state to pacman
	Distance to closest food * -8.1
	eat-food if next cell don't have ghost then eat that food pellet will have value 25
	
	distance to ghost BFS shortest distance to ghost *
	the no of food remains
		food pellets remains * -2.9 when  pacman is in fear mode 
		food pellets remains * -0.7 when pacman not in fear mode
	
	distance to power pellet * -47 since the value is negative it always tries to decrease this value and hence appraoahces 
	towards power pellet
	 
	distance to ghost if it is in chase mode
		distance*-4f
	else
		distance*58
		
		
rewards:
reward for completing food is +1600
reward for food pellets remaining is-100
reward for food eating food pellets remaining is +30
reward for food eating power  pellets remaining is +200
reward for going into ghost cell is -200
reward for going into ghost cell in fear mode is +200	
reward for going eating fearful ghost in fear mode is +1000
reward for eaten by   ghost  is -1000

Program flow 

	for each state it computes the maximum Q values for all possible states with above feature vectors and get the direction i=hich has maximum q value
	and takes that action.
	Once it takes that action it updates the weight of the features vectors for that transition and proceed in the curret
	state with updated weights.
	This process will undergo for the no of training episodes once entered in final simulation run learning rate 
	and epsilon value will be zero and no weights are updated in this final simulation
	
 
*/






import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import javax.swing.JFileChooser;


import pacsim.FoodCell;
import pacsim.GhostCell;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacMode;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;
import pacsim.PowerCell;
import pacsim.WallCell;

public class PacSimReplan1 implements PacAction {
   
   
   public static float avgMoves = 0;
   public static int won = 0;
   public static int lost = 0;	
   public static float interval = 100;
   public static int totalAvgMoves = 0;
   public static int totalWon = 0;
   public static int totalLost = 0;
   public static float winpercentage = 0.0f;
   
   
   public static float carry = 0f;
   public static float factor=100f;
   
   private Point target;
   public static int epochs = 0;
   public static int epochCounter = 0;
   PacSim sim = null;
   
   public static boolean val;
   public int nFeatures = 3;
   public HashMap<PacFace,Point> dirtoloc = new HashMap<PacFace,Point>();
   
   public HashSet<PacCell> allWallLocations  = new HashSet<PacCell>(); 
   public ArrayList<PacCell>  loc = new ArrayList<PacCell>();
   public ArrayList<PacFace>  dir = new ArrayList<PacFace>();
   public HashMap<String,Double> weights = new HashMap<String,Double>();
   public Float epsilon = 0.05f;
   
   public Float alpha = 0.2f;
   public Float gamma = 0.8f; 
   public Float OldQValue = 0.0f;
   public HashMap<String,Float> oldfVector = new HashMap<String,Float>();
   public double oldReward = 0.0f;
   public int movecounter = 0;
   ArrayList<Point> twoLocations = new ArrayList<Point>();
   public PacSimReplan1( String fname,int trainingEpisodes ) {
	  // System.out.println("simulation button clicked");
	  System.out.println("episodes is"+trainingEpisodes);
      sim = new PacSim(fname,trainingEpisodes+1);
    
   
      epochCounter = 0; 
      //System.out.println("simulation button clicked");
      setDirtoLoc();
      setLoctoDir();
      setUp();
      sim.init(this);
      //System.out.println(" new run");
   }
   public void setUp()
   {
	   //System.out.println("new run");
	   epochCounter = 0;
	   weights.put("Neighbour-Ghost-count", 0.0);
	   weights.put("closest-food", 0.0);
	   weights.put("eat-food", 0.0);
	   weights.put("food-remain", 0.0);
	   weights.put("power-remain", 0.0);
	   weights.put("ghost-distance", 0.0);
	   weights.put("power-distance",0.0);
	   weights.put("sghost",0.0);
	   System.out.print("Alpha   = "+alpha+"  ");
	   System.out.print("Gamma   = "+gamma+"  ");
	   System.out.print("Epsilon = "+epsilon+"\n  ");
	   twoLocations.add(new Point(1,0));
	   twoLocations.add(new Point(-1,0));
	   twoLocations.add(new Point(0,1));
	   twoLocations.add(new Point(0,-1));
	   twoLocations.add(new Point(2,0));
	   twoLocations.add(new Point(-2,0));
	   twoLocations.add(new Point(0,2));
	   twoLocations.add(new Point(0,-2));
   }
   
   public void setDirtoLoc()
   {
	   loc.add(new PacCell(1, 0));
	   loc.add(new PacCell(-1, 0));
	   loc.add(new PacCell(0, -1));
	   loc.add(new PacCell(0, 1));
	   
	   dirtoloc.put(PacFace.E, new Point(1, 0));
	   dirtoloc.put(PacFace.W, new Point(-1, 0));
	   dirtoloc.put(PacFace.N, new Point(0, -1));
	   dirtoloc.put(PacFace.S, new Point(0, 1));
	   
	   return;
   }
   
   
   
   
   public void setLoctoDir()
   {
	   dir.add(PacFace.E);
	   dir.add(PacFace.W);
	   dir.add(PacFace.N);
	   dir.add(PacFace.S);
	   return;
   }
   
   
   //get the corresponding location for a given direction
   public PacCell getDirtoLoc(int pos)
   {
	   return loc.get(pos);
   }
   //get the corresponding  direction for a given location
   public PacFace getLoctoDir(int pos)
   {
	   return dir.get(pos);
   }
   
   
   public static void main(String[] args) {   
      
      String fname ="";
      
      if( args.length > 1) {
         fname = args[ 0 ];
         epochs = Integer.parseInt(args[1]);
      }
      else if (args.length>0)
      {
    	  fname = args[0];
    
    	  
      }
       {
         JFileChooser chooser = new JFileChooser(
               new File("/Users/glinosd/Desktop/Pacsim"));
         int result = chooser.showOpenDialog(null);

         if( result != JFileChooser.CANCEL_OPTION ) {
            File file = chooser.getSelectedFile();
            fname = file.getName();
         }
      }   
      new PacSimReplan1( fname,epochs);
    
   }

   public void init() {
	   if(epochCounter>epochs)
	   {
	   }
	   if(epochCounter>0)
	   {
	
	   }
	   avgMoves += movecounter;
	   
	   if(epochs!= 0 && epochCounter!=0 && epochCounter%(int)(interval)==0)
	   {
		   System.out.println("weihgts are"+"NN count"+weights.get("Neighbour-Ghost-count")+","+
		   "cf "+weights.get("closest-food")+",ef "+weights.get("eat-food")
		   +  " fr ,"+weights.get("food-remain")+", gd "+weights.get("ghost-distance")+", sg "+weights.get("sghost"));

		   avgMoves = (float)(avgMoves/interval);
	
		   System.out.print("won "+won+", ");
		   lost = 100-won;
		   System.out.print("Lost "+lost+", ");
		   System.out.print("Avegrage Moves"+avgMoves+", ");
		  
		   
		   totalAvgMoves +=avgMoves;
		   totalWon +=won;
		   totalLost += lost;
		   avgMoves= 0;
		   
	   }
	   
	   movecounter = 0;
      target = null;
      
      epochCounter++;
   }
   

   double getnoofghosts(PacCell cell1,PacCell[][] grid)
   {
	   double value = 0;
	   for(int i =0;i<dir.size();i++)
	   {
		   Point loc = dirtoloc.get(dir.get(i));
		   int curx = (int) (cell1.getX());
		   int cury = (int) (cell1.getY());
		   if(grid[curx][cury] instanceof GhostCell)
		   {
			   value+=1;
		   }
	   }
	   return value;
   }
   
   double getfour(PacCell cell1,PacCell[][] grid)
   {
	   double value = 0.0;
	   GhostCell p =PacUtils.nearestGhost(new Point(cell1.getX(),cell1.getY()),grid);
	   if(p!=null)
	   {
		   value = mindistancetoghost(new Point(cell1.getX(),cell1.getY()),grid);

				   if (value<=3.0)
				   {
					   value = 1.0;
				   }
	   }

	   return value;
   }
   
   double getOne(PacCell cell1,PacCell[][] grid)
   {
	   double value = 0.0;
	   GhostCell p =PacUtils.nearestGhost(new Point(cell1.getX(),cell1.getY()),grid);
	   if(p!=null)
	   {
		   value = PacUtils.manhattanDistance(new Point(cell1.getX(),cell1.getY()), new Point(p.getX(),p.getY()));
				   if (value<=1.0)
				   {
					   value = 1.0;
				   }
	   }

	   return value;
   }
   
   //compute get possible actions  for this state
   ArrayList<PacFace> getPossibleActions(PacCell[][] grid)
   {
	  PacmanCell cell1   = PacUtils.findPacman(grid);
	  ArrayList<PacCell>   neigh = new ArrayList<PacCell>();
	  
	  PacCell loc1 = new PacCell(1, 0);
	   neigh.add(loc1);
	   neigh.add(new PacCell(-1,0));
	   neigh.add(new PacCell(0,-1));
	   neigh.add(new PacCell(0,1));
  
	   
	   ArrayList<PacFace> possibleactions = new ArrayList<PacFace>();
	   possibleactions.clear();
	   for(int i =0;i<neigh.size();i++)
	   {
		   int curx = cell1.getX()+neigh.get(i).getX();
		   int cury = cell1.getY()+neigh.get(i).getY();
		   if (!(grid[curx][cury] instanceof WallCell) && curx>-1 && curx<grid.length && cury>-1 && cury<grid[0].length)
		   {
		   		possibleactions.add(dir.get(i));
		   }
	   }
	return possibleactions;
   }
   
   
   //stores the node for Breadth first search which contains loc and count from that location to destination
   class NodeforBfs
   {
	   Point loc;
	   int count;
	   NodeforBfs(Point loc,int count)
	   {
		   this.count = count;
		   this.loc = loc;
	   }
   }
   
   // find the minimum distance between 2 points in the maze considering the wall locations it uses breadth first search
   // to find minimum distance between 2 points
   public int mindistance(Point sLoc, PacCell[][] grid)
   {
	   
	   Queue<NodeforBfs> q1 = new LinkedList<NodeforBfs>();
	   
	   q1.add(new NodeforBfs(sLoc, 0));
	   HashSet<Point> visited = new HashSet<Point>();
	   while(!q1.isEmpty())
	   {
		   NodeforBfs currentNode = q1.poll();
		  if (visited.contains(currentNode.loc))
		  		continue;
		  visited.add(currentNode.loc);
		  
		  if(grid[(int)currentNode.loc.getX()][(int)currentNode.loc.getY()] instanceof FoodCell)
		  {
			  //System.out.println("foodcellfound");
			  return currentNode.count; 
		  }
		   //get all the successors from the current location in all directions and add to the queue
		   ArrayList<Point>   neigh = new ArrayList<Point>();
		   neigh.add(new Point(1,0));
		   neigh.add(new Point(-1,0));
		   neigh.add(new Point(0,1));
		   neigh.add(new Point(0,-1));
		   
		   ArrayList<NodeforBfs> allSuccessors = new ArrayList<NodeforBfs>();
		   //nodes++;
		   for(int i =0;i<neigh.size();i++)
		   {
			   int x= currentNode.loc.x;
			   int y= currentNode.loc.y;
			   int nextX = 0,nextY = 0;
			   if(x+neigh.get(i).getX()>-1 && x+neigh.get(i).getX()<grid.length && y+neigh.get(i).getY()>-1 && y+neigh.get(i).getY()<grid[0].length)
			   {
				   nextX = (int) (x+neigh.get(i).getX());
			   	   nextY = (int) (y+neigh.get(i).getY());
			   	   if(!(grid[nextX][nextY] instanceof WallCell))
				   {
					   NodeforBfs n = new NodeforBfs(new Point(nextX,nextY),1);
					   allSuccessors.add(n);
				   }
			   }
	   
		   }
		   
		   //Now add the successors to the queue if they are not present in visited 
		   for(NodeforBfs succ : allSuccessors)
		   {
			   if(!visited.contains(succ.loc))
			   {
				   
				   NodeforBfs n5 = new NodeforBfs(succ.loc,currentNode.count+succ.count); 
				   q1.add(n5);
			   }
		   }
		   
		   
	   }
	   return 0;
   }
   
   
   public int mindistancetoghost(Point sLoc, PacCell[][] grid)
   {
	   
	   Queue<NodeforBfs> q1 = new LinkedList<NodeforBfs>();
	   
	   q1.add(new NodeforBfs(sLoc, 0));
	   HashSet<Point> visited = new HashSet<Point>();
	   while(!q1.isEmpty())
	   {
		   NodeforBfs currentNode = q1.poll();
		  if (visited.contains(currentNode.loc))
		  		continue;
		  visited.add(currentNode.loc);
		  
		  if(grid[(int)currentNode.loc.getX()][(int)currentNode.loc.getY()] instanceof GhostCell)
		  {
			  //System.out.println("foodcellfound");
			  return currentNode.count; 
		  }
		   //get all the successors from the current location in all directions and add to the queue
		   ArrayList<Point>   neigh = new ArrayList<Point>();
		   neigh.add(new Point(1,0));
		   neigh.add(new Point(-1,0));
		   neigh.add(new Point(0,1));
		   neigh.add(new Point(0,-1));
		   
		   ArrayList<NodeforBfs> allSuccessors = new ArrayList<NodeforBfs>();

		   for(int i =0;i<neigh.size();i++)
		   {
			   int x= currentNode.loc.x;
			   int y= currentNode.loc.y;
			   int nextX = 0,nextY = 0;
			   if(x+neigh.get(i).getX()>-1 && x+neigh.get(i).getX()<grid.length && y+neigh.get(i).getY()>-1 && y+neigh.get(i).getY()<grid[0].length)
			   {
				   nextX = (int) (x+neigh.get(i).getX());
			   	   nextY = (int) (y+neigh.get(i).getY());
			   	   if(!(grid[nextX][nextY] instanceof WallCell))
				   {
					   NodeforBfs n = new NodeforBfs(new Point(nextX,nextY),1);
					   allSuccessors.add(n);
				   }
			   }
	   
		   }
		   
		   //Now add the successors to the queue if they are not present in visited 
		   for(NodeforBfs succ : allSuccessors)
		   {
			   if(!visited.contains(succ.loc))
			   {
				   
				   NodeforBfs n5 = new NodeforBfs(succ.loc,currentNode.count+succ.count); 
				   q1.add(n5);
			   }
		   }
		   
		   
	   }
	   return 0;
   }
   
   public int mindistancetopower(Point sLoc, PacCell[][] grid)
   {
	   
	   Queue<NodeforBfs> q1 = new LinkedList<NodeforBfs>();
	   
	   q1.add(new NodeforBfs(sLoc, 0));
	   HashSet<Point> visited = new HashSet<Point>();
	   while(!q1.isEmpty())
	   {
		   NodeforBfs currentNode = q1.poll();
		  if (visited.contains(currentNode.loc))
		  		continue;
		  visited.add(currentNode.loc);
		  
		  if(grid[(int)currentNode.loc.getX()][(int)currentNode.loc.getY()] instanceof PowerCell)
		  {

			  return currentNode.count; 
		  }
		   //get all the successors from the current location in all directions and add to the queue
		   ArrayList<Point>   neigh = new ArrayList<Point>();
		   neigh.add(new Point(1,0));
		   neigh.add(new Point(-1,0));
		   neigh.add(new Point(0,1));
		   neigh.add(new Point(0,-1));
		   
		   ArrayList<NodeforBfs> allSuccessors = new ArrayList<NodeforBfs>();
		   //nodes++;
		   for(int i =0;i<neigh.size();i++)
		   {
			   int x= currentNode.loc.x;
			   int y= currentNode.loc.y;
			   int nextX = 0,nextY = 0;
			   if(x+neigh.get(i).getX()>-1 && x+neigh.get(i).getX()<grid.length && y+neigh.get(i).getY()>-1 && y+neigh.get(i).getY()<grid[0].length)
			   {
				   nextX = (int) (x+neigh.get(i).getX());
			   	   nextY = (int) (y+neigh.get(i).getY());
			   	   if(!(grid[nextX][nextY] instanceof WallCell))
				   {
					   NodeforBfs n = new NodeforBfs(new Point(nextX,nextY),1);
					   allSuccessors.add(n);
				   }
			   }
	   
		   }
		   
		   //Now add the successors to the queue if they are not present in visited 
		   for(NodeforBfs succ : allSuccessors)
		   {
			   if(!visited.contains(succ.loc))
			   {
				   
				   NodeforBfs n5 = new NodeforBfs(succ.loc,currentNode.count+succ.count); 
				   q1.add(n5);
			   }
		   }
		   
		   
	   }
	   return 0;
   }
   
   
   
   
   //compute feature vectors of the all actions of the state
   //it consists of hashhMap with a feature name and its corresponding value
   //if at that state if the feature is not present then the corresponding value is zero
   HashMap<String,Float> getFeatureVector (PacCell[][] grid,PacFace action)
   {
	   	   PacmanCell cell1   = PacUtils.findPacman(grid);
	   	   HashMap<String,Float> featureVector = new HashMap<String,Float>();

		   Point loc = dirtoloc.get(action);
		   int curx = (int)(cell1.getX()+loc.getX());
		   int cury = (int)(cell1.getY()+loc.getY());
		   float value = (float) getnoofghosts(new PacCell(curx,cury), grid);
		   

		   float value1 = (float) (2*value/10.0);

		   featureVector.put("Neighbour-Ghost-count", value1);
		   Point curCell = new Point(curx,cury);
		   if(PacUtils.foodRemains(grid))
		   {
			   float  dist=   (float)mindistance(curCell, grid);

			  if(epochCounter>=2 && action==PacFace.W && dist == 3.0f)
			   {

			   }


			   float dist1 =  -8.1f*(float) ((float) (dist)/1000.0);

			   featureVector.put("closest-food", dist1);
			   if(epochCounter<2)
			   {

			   }
		   }
		   else
		   {
			   featureVector.put("closest-food", 0.0f);
		   }
		  
		   //feature3 is the food dot eatable
		   double v1 = getfour(cell1, grid);

		   if((v1==0)&& grid[curx][cury] instanceof FoodCell)
		   {
			   value = (float) (25f);
			   featureVector.put("eat-food", value);
		   }
		   else
		   {
			   featureVector.put("eat-food", 0.0f);
		   }
//		   if(PacUtils.numPower(grid)>0)
//		   {
//			   double value7 = PacUtils.numPower(grid);
//			   value7 = -value7/100f;
//			   featureVector.put("power-remain", (float) value7);
//		   }
//		   else
//		   {
//			   featureVector.put("power-remain", 0f);
//		   }
		   if(PacUtils.foodRemains(grid))
		   {
			   GhostCell value7 = PacUtils.nearestGhost(curCell, grid);
			   
			   //System.out.println("food remains is"+PacUtils.numFood(grid));
			    float value6 = 0.0f;
			    value6 = 2*PacUtils.numFood(grid);
			       List<Point> l1 = PacUtils.findGhosts(grid);
			       double d11 = 0.0f;
			      if(l1.size()>1 && l1.get(0)!=null &&  l1.get(1)!=null)
			      {
			    	  d11 = PacUtils.manhattanDistance(l1.get(0), l1.get(1));
//				    	  double v1 = getOne(new PacCell((int)l1.get(0).getX(), (int)l1.get(0).getY()), grid);
//				    	  double v2 = getOne(new PacCell((int)l1.get(0).getX(), (int)l1.get(0).getY()), grid);
//				    	  
//				    	  if(v1==1 && v2==1)
//				    	  {
//				    		  reward = reward+100f;
//				    	  }
			      }

			    if (value7!=null && value7.getMode().equals(PacMode.FEAR)||d11>10.0)
				   {
			    	value6 = -2.9f*(value6)/1000.0f;
				   } 
			    else if (value7!=null && (value7.getMode().equals(PacMode.CHASE) || value7.getMode().equals(PacMode.SCATTER)))
			    {
			    	value6 = -0.70f*(value6)/1000.0f;
			    }
			    
			   featureVector.put("food-remain", value6);
		   }
		   Point pcell   = PacUtils.nearestPower(new Point(curx,cury),grid);
		   
		   if(pcell!=null && v1==1.0)
		   {
			   //System.out.println("ghost is not near");
			   //float d6 = (float)PacUtils.manhattanDistance(new Point(curx,cury),pcell);
			   float d6 = (float)mindistancetopower(new Point(curx,cury),grid);
			   d6 = -47f*(float) ((float) (d6)/1000.0);
			   featureVector.put("power-distance",d6);
		   }
		   else
		   {
			   featureVector.put("power-distance",0f);
		   }
		   
		   GhostCell value7 = PacUtils.nearestGhost(curCell, grid);
		   	List<Point> l1 = PacUtils.findGhosts(grid);
		   if(value7!=null)
		   {
			   Point gcell = new Point(value7.getX(),value7.getY());
				 
				 if (value7.getMode().equals(PacMode.FEAR))
				 {

					 float dist8 = (float)PacUtils.manhattanDistance(new Point(curx,cury),gcell);
					 dist8 =    -4f*dist8/1000.0f;

					 featureVector.put("ghost-distance" , 0.0f);
				 }
				 else if(value7.getMode().equals(PacMode.CHASE))
				 {

					  gcell = new Point(value7.getX(),value7.getY());
					  float dist5 = (float)mindistancetoghost(new Point(curx,cury), grid);
					  dist5 =  (float) ((float) (58.0f*dist5)/1000.0f);
					 featureVector.put("ghost-distance",dist5);
			 }
			   
		   }
		   
		 
		   
		return featureVector;
   }
   
   
   
   //compute Q value based on feature vectors of the (state,Action)
   float getQvalue(PacCell[][] grid,PacFace action)
   {
	   HashMap<String, Float> fVector = getFeatureVector(grid, action);
	   Float qvalue = 0.0f;
	   for (String k : fVector.keySet()) 
	   {
		   

		    qvalue  = qvalue+ (float) (weights.get(k)*fVector.get(k));
		}
	   
	   return qvalue;
	   
   }
     
   //compute optimumAction()
   PacFace optimumAction(PacCell[][] grid)
   {
	   ArrayList<PacFace> possibleActions = getPossibleActions(grid);
	   //System.out.println("possible actions are"+possibleActions);
	   PacFace maxAction = null;
	   double maxQvalue = Integer.MIN_VALUE;
	   //System.out.println("maxQueue"+Integer.MIN_VALUE);
	   for (int i = 0;i<possibleActions.size();i++)
	   {
		   
		   double val = (double)getQvalue(grid, possibleActions.get(i));
		   //int res = Float.compare((float)maxQvalue, val); 
		   //System.out.println("action is"+possibleActions.get(i)+"val is"+val);
		   double res = Math.max(maxQvalue, val);
		   val = val/10000.0;
		   //System.out.println("res is"+res);
		   //System.out.println("");
		   //System.out.println(Double.compare(val,maxQvalue));
		   if(Double.compare(val,maxQvalue)>=0)
		   {
			   //System.out.println();
			   maxQvalue = val;
			   maxAction = possibleActions.get(i);  
			   
		   }
		   else
		   {
//			   if (Math.random()< (double)epsilon)
//			   {
//				   //choose random action
//				   //System.out.println("choosing random action");
//				   ArrayList<PacFace> actions = getPossibleActions(grid);
//				   
//				   Random random = new Random();
//				   int randomNumber = random.nextInt(actions.size());
//				   maxAction = actions.get(randomNumber);
//			   }
//			   //double res = res/100f;
//			   
//			   System.out.println("greater");
		   }
		  
	   }
	   //System.out.println("action is"+maxAction);
	   return maxAction;
   }
   
   //compute getAction
   PacFace getAction(PacCell[][] grid)
   {
	   PacFace action = null;
	   if (Math.random()< (double)epsilon)
	   {
		   //choose random action
		   //System.out.println("choosing random action");
		   ArrayList<PacFace> actions = getPossibleActions(grid);
		   
		   Random random = new Random();
		   int randomNumber = random.nextInt(actions.size());
		   action = actions.get(randomNumber);
	   }
	   else
	   {
		   //choose optimum action way
		   action = optimumAction(grid);
	   }
	   
	   return action;
   }
  
   float getMaxQvalue(PacCell[][] grid)
   {
	   ArrayList<PacFace> possibleActions = getPossibleActions(grid);
	   Float maxQvalue = Float.MIN_VALUE;
	   
	   for (int i = 0;i<possibleActions.size();i++)
	   {
		   
		   Float val = getQvalue(grid, possibleActions.get(i));
		   if(Float.compare(Math.max(maxQvalue, val), val)==0)
		   {
			   maxQvalue = val;
			   //maxAction = possibleActions.get(i);
		   }
	   }
	   
	   if(maxQvalue == Float.MIN_VALUE)
		   return 0.0f;
	   else
		   return maxQvalue;
   }
   
   
   public void updateWeights(PacCell[][] grid,double oldreward,float oldQvalue,HashMap<String,Float> fVector)
   {
	   float qnextState = getMaxQvalue(grid);
	   
	   double reward = oldReward;
	   //difference=(r+γmaxa′Q(s′,a′))−Q(s,a)
	   	   
	   double difference = (reward+(gamma *qnextState)) -oldQvalue; 
	   for (String k : fVector.keySet()) 
	   {
		   
		   Double uvalue = weights.get(k)+alpha*difference*fVector.get(k); 
		   weights.put(k, uvalue);
		}
   }
   
   public double getReward(PacCell[][] grid,PacFace action)
   {

	   double reward = 0.0;
	   PacmanCell cell1   = PacUtils.findPacman(grid);
	   int curx = (int) (cell1.getX()+dirtoloc.get(action).getX());
	   int cury = (int) (cell1.getY()+dirtoloc.get(action).getY());
       List<Point> l1 = PacUtils.findGhosts(grid);
       if(!PacUtils.foodRemains(grid))
	   {

		   reward = reward+1600;
	   }
	   if(sim.gameOver()&& PacUtils.foodRemains(grid))
	   {

		   reward = reward-100;
	   }
	   if(PacUtils.food(curx, cury, grid))
	   {
		   reward = reward+30.0;
	   }
	   else if(grid[curx][cury] instanceof PowerCell)
	   {
		   reward = reward+200.0;
	   }
	   else if(sim.ghostEaten())
	   {
		   reward = reward+1000;
	   }
	   else if(sim.pacmanEaten())
	   {
		   reward = reward-1000;
	   }
	   else if(grid[curx][cury] instanceof GhostCell)
	   {
		 GhostCell   g = (GhostCell)grid[curx][cury];
		 if (g.getMode().equals(PacMode.FEAR))
		 {
			 reward = reward+200.0;
		 }
		 if (g.getMode().equals(PacMode.CHASE))
		 {
			 reward = reward-200.0f;
		 }
	   }
	   
	   //update living reward
	  // reward = reward -1;
	   return reward;
   }
   
   @Override
   public PacFace action( Object state ) 
   {
	   
	   
	   PacFace curAction = null;
	   if (epochCounter>factor && ((epochCounter%100)==(epochs+1+carry)%100))
	   {
		   //System.out.println("for simulation  purpose setting to zero");
		   
		   carry = carry+1;
		   alpha = 0.0f;
		   epsilon = 0.0f;
		   
		   //System.out.println("Intial called for"+epochCounter+"alpha  = :"+alpha+"epsilon :"+epsilon);
		   
		   System.out.print("    total won   "+totalWon);
		   System.out.print("    total lost  "+totalLost);
		   winpercentage = 100*totalWon/(float)factor;
		   System.out.println("   win percentage  "+winpercentage);
		   System.out.print("total avg moves"+totalAvgMoves);
		   factor = factor+100;
		   won = 0;
		   lost = 0;
		  // return null;
	   }
	   else if(alpha==0.0f && epsilon==0.0f)
	   {
		   if((int)winpercentage>65)
		   {
			   //System.out.println("resettingback  for training purpose");  
			   alpha = 0.05f;
			   epsilon=0.001f;
		   }
		   else
		   {
			   //System.out.println("resettingback  for training purpose");  
			   alpha = 0.2f;
			   epsilon=0.05f;
		   }
	   }
	   
      PacCell[][] grid = (PacCell[][]) state;
      
      PacmanCell pc = PacUtils.findPacman( grid );
      //check for game over
      
      if(movecounter>0 && pc!=null)
	   {
    	  
		   updateWeights(grid, oldReward, OldQValue, oldfVector);
	   }
    //check if the total no of dots are eaten in this epoch
      if(!PacUtils.foodRemains(grid))
      {
    	//  System.out.println("food eaten");
      }
     
      if(!(epochCounter>factor && ((epochCounter%100)==(epochs+1+carry)%100)) && 
    		  !PacUtils.foodRemains(grid) && sim.gameOver())
      {
    	  won+=1;
    	  
      }
     
      PacFace newFace = null;
      
      if( pc != null ) {
                   
         ArrayList<PacFace> possibleactions= getPossibleActions(grid);
         //System.out.println("possible actions are"+possibleactions);
         curAction = getAction(grid);
         //System.out.println("cur action is"+curAction);
         
         double reward = getReward(grid,curAction);
         
         OldQValue = getQvalue(grid, curAction);
         if (epochCounter<2)
         {
        	 
         }
         oldReward = reward;
         oldfVector = getFeatureVector(grid, curAction);
         movecounter+=1;
         
         
         
         }
      return curAction;

   }
}
