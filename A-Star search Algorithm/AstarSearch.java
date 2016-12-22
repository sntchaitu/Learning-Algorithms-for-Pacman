
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JFileChooser;








import javax.swing.text.html.HTMLDocument.Iterator;

import pacsim.FoodCell;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;
import pacsim.WallCell;

/**
 * UCF
 * CAP 5636-Advanced Artificial Intelligence
 * 2nd semester
 * PID:3667008
 * Author S.Santhosh Chaitanya


 * Below program will implement Astar search algorithm to eat all the food dots in the maze.
 * 
 * From the starting position node neighbours are added to the priority queue based on the total cost
 * 
 * Node class structure
 * 
 * 	Node will contains the state arrayList,route arrayList,and integer priority is the total cost g(n)+h(n)
 *  state will inturn contain position of each neighbour and state of food pellets,which is represented as 
 *  a 2d arraylist, at the neighbour
 *
 * for each neighbour position all the values of Node class are updated and loaded into the priority queue
 * untill the goal state is reached each node with high priority will be removed from the queue 
 * and all its neighbours are stored into the queue with their respective priorities
 * 
 * Goal state is reached when all the food pellets are consumed that is food matrix contains all zeros
 * 
 * Total cost
 * 
 * Total cost is adding  g(n) +h(n)
 * g(n) = sum of priority ie.cost of getting  from initial node to n and
 * h(n) =  heuristic cost  estimate of the cost to get from n to any goal node
 * 
 * For heuristic cost estimate, we computed minimum spanning from a neighbour to all the vertices of food dots
 * Minimum spanning tree cost is computed using kruskal's algorithm
 * kruskal's algorithm will find out the edges that are of minimum weight from starting position to end position
 * without forming a cycle
 * 
 * While calculating distance of Minimum spanning tree the minimum distance between 2 points are calculated 
 * by using breadth first search method
 *
 *  
 *Breadth First Node class structure
 *
 *	stores the node for Breadth first search which contains loc and count from that location to destination
 *
 * 
 * 
 * and we store the sum of g(n) +h(n) for all neighbours that we have expanded into priority variable and store then in priority queue
 *Nodes of neighbours are expanded only in the target direction of the path which has low cost of g(n)+h(n)
 *   
 * Hence when each node,which has lower cost towards destination is popped from the priority queue
 * until the goal state is reached.
 * 
 * 
 *when the get Action method  is called first it calculates entire path and stores in the array list
 *and also calculates the direction for the solution path and stores in the array list
 *Until all solution path is covered every time when the getAction is called it will send the required PACFACE(direction) 
 *command as return variable
 */




///////////Actual Progam starts here/////////////////////////////////////////
public class AstarSearch implements PacAction {
   public  long nodes = 0; 
   private Point target;
   //int nodes = 0;
   int counter = 0;
   ArrayList<Point> path  = new ArrayList<Point>();
   ArrayList<PacFace> direction = new ArrayList<PacFace>();
   public AstarSearch( String fname ) {
      PacSim sim = new PacSim( fname );
      sim.init(this);
   }
  
   public static void main( String[] args ) {   
      
      
      
      String fname ="";
      if( args.length > 0 ) {
         fname = args[ 0 ];
      }
      else {
         JFileChooser chooser = new JFileChooser(
               new File("/Users/glinosd/Desktop/Pacsim"));
         int result = chooser.showOpenDialog(null);

         if( result != JFileChooser.CANCEL_OPTION ) {
            File file = chooser.getSelectedFile();
            fname = file.getName();
         }
      }   
      new AstarSearch( fname );
   }

   @Override
   public void init() {
      target = null;
   }
   class routendir
   {
	  public Point route;
	  public PacFace direction;
	   routendir(Point route,PacFace direction)
	   {
		   this.route = route;
		   this.direction = direction;
	   }
	   
   }
 //Comparator anonymous class implementation
   public static Comparator<Edge> costComparator1 = new Comparator<Edge>()
	{
        
       @Override
       public int compare(Edge n1, Edge n2) {
           return (int) (n1.cost- n2.cost);
       }
   };
   
   
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
   public int mindistance(Point sLoc,Point eLoc, PacCell[][] grid,HashSet<Point> allWallLocations)
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
		  if(currentNode.loc.equals(eLoc))
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
			   	   if(!allWallLocations.contains(new Point(nextX,nextY)))
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
   
   //below method will compute the h cost from  each neighbour to all the food vertices
   public    int computetotalHcost(ArrayList<ArrayList<ArrayList<Integer>>> state,PacCell[][] grid,HashSet<Point> allWallLocations)
   {
	   if(getOneCount(state.get(1))==0)
	   {
		   return 0;
	   }
	   int totalcost = 0;
	   
	   //Frame all the locations of food locations
	  
	   ArrayList<Point> vertices = new ArrayList<Point>();
	   for(int i =0;i<state.get(1).size();i++)
	   {
		   for(int j = 0;j<state.get(1).get(i).size();j++)
		   {
			   if(state.get(1).get(i).get(j)==1)
			   {
				   vertices.add(new Point(i,j));
			   }
		   }
	   }
	   
	   //compute the   distance between successor position and each food point
	   // and get the minimum distance of all the distances
	   Point sLoc = new Point(state.get(0).get(0).get(0).intValue(),state.get(0).get(0).get(1).intValue());
	   
	   int cost = Integer.MAX_VALUE;
	   for(int i =0;i<vertices.size();i++)
	   {
		   int temp =  mindistance(sLoc,vertices.get(i),grid,allWallLocations);
		   if(cost>temp)
		   {
			   cost = temp;
		   }
	   }
	   // from distance arrayList get the minimum distance
	   totalcost = cost;
	   
	 //put all the edges into priority queue
	   PriorityQueue<Edge> queue = new PriorityQueue<Edge>(costComparator1);
	   
	   // take a vertex from vertices list
	   Point c_vertex = vertices.remove(0);
	   while(!vertices.isEmpty())
	   {
		   
		   for(int i =0;i<vertices.size();i++)
		   {
			  cost = mindistance(c_vertex, vertices.get(i), grid,allWallLocations);
			  Edge e1= new Edge(c_vertex,vertices.get(i),cost);
			  queue.add(e1);
		   }
		   
		   while (true)
		   {
			   int index = -1;
			   Edge t1 = queue.poll();
			   Point dst = t1.B;
			   cost = t1.cost;
			   for(int i =0;i<vertices.size();i++)
			   {
				   if(vertices.get(i).equals(dst))
				   {
					   index = i;
					   break;
				   }
			   }
			   if(index!=-1)
			   {
				   totalcost += cost;
				   c_vertex = vertices.get(index);
				   vertices.remove(index);
				   break;
			   }
		   }
		   
	   }
	   return totalcost;
   }
   //class to store frame the  edges between 2 food vertices
   class Edge
   {
	   Point A;
	   Point B;
	   Integer cost;
	   Edge(Point A,Point B,Integer cost)
	   {
		   this.A = A;
		   this.B = B;
		   this.cost = cost;
		   
	   }
   }
   
   //Node will contains the state arrayList,route arrayList,and integer priority is the total cost g(n)+h(n)
   //state will inturn contain position of each neighbour and state of food pellets,which is represented as 
   //a 2d arraylist, at the neighbour 
   									
   class Node
   {
	   ArrayList<ArrayList<ArrayList<Integer>>> state= new ArrayList<ArrayList<ArrayList<Integer>>>();
	   ArrayList<Point> route = new ArrayList<Point>();
	   //ArrayList<PacFace> direction = new ArrayList<PacFace>();
	   Integer priority;
	   Node(ArrayList<ArrayList<Integer>> loc,ArrayList<ArrayList<Integer>> foodSet,ArrayList<Point> route,Integer priority)
	   {
		   
		   //set the position of the state
		   ArrayList<Integer> loc1 = new ArrayList<Integer>();
		   loc1.add(0, loc.get(0).get(0));
		   loc1.add(1, loc.get(0).get(1));
		   ArrayList<ArrayList<Integer>> fpos = new ArrayList<ArrayList<Integer>>();
		   fpos.add(loc1);
		   state.add(0,fpos);
		   
		   
		   // set the food matrix arrayList to the list
		   ArrayList<ArrayList<Integer>> fm= new ArrayList<ArrayList<Integer>>(); 
		   for(int i =0;i<foodSet.size();i++)
		   {
			   ArrayList<Integer> tz = new ArrayList<Integer>();
			   for(int j = 0;j<foodSet.get(i).size();j++)
			   {
				   tz.add(foodSet.get(i).get(j));
			   }
			   fm.add(tz);
		   }
		   
		   state.add(1,fm);
  
		   
		   //all route to the arraylist
		   this.route.clear();
		   this.route.addAll(route);
//		   this.direction.clear();
//		   this.direction.addAll(direction);
		   this.priority = priority;
		   
	   
	   }
	   
	   
   }
 //Comparator anonymous class implementation
   public static Comparator<Node> costComparator = new Comparator<Node>(){
        
       @Override
       public int compare(Node n1, Node n2) {
           return (int) (n1.priority- n2.priority);
       }
   };
   //count the no of ones in the food matrix 1 represent vertices of food
   public int getOneCount(ArrayList<ArrayList<Integer>> foodMatrix)
   {
	   int count = 0;
	   for(int i =0;i<foodMatrix.size();i++)
	   {
		   for(int j = 0;j<foodMatrix.get(i).size();j++)
		   {
			   if(foodMatrix.get(i).get(j)==1)
			   {
				   count++;
			   }
		   }
	   }
	   return count;
   }
   
   
   public static ArrayList<ArrayList<Integer>> getCopy(ArrayList<ArrayList<Integer>> input) {

	    ArrayList<ArrayList<Integer>> copy = new ArrayList<ArrayList<Integer>>();

	    for(int i = 0; i < input.size(); i++) {
	        ArrayList<Integer> line = new ArrayList<Integer>();

	        for(int j = 0; j < input.get(i).size(); j++) {
	            line.add(input.get(i).get(j));
	        }

	        copy.add(line);
	    }
	    return copy;
	}
   
   
   // get all the successors of a nodes and frame the node data structure
   ArrayList<Node> getSuccessors(ArrayList<ArrayList<ArrayList<Integer>>> state,PacCell[][] grid,HashSet<Point> allWallLocations)
   {
	   
	   if(nodes%1000==0  && nodes>0)
	   {
		  System.out.println("Nodes Expanded: "+nodes); 
	   }
	   
	   nodes++;
	   ArrayList<Point>   neigh = new ArrayList<Point>();
	   
	   neigh.add(new Point(1,0));
	   neigh.add(new Point(-1,0));
	   neigh.add(new Point(0,1));
	   
	   neigh.add(new Point(0,-1));
	   
	   ArrayList<Node> allSuccessors = new ArrayList<Node>();
	   
	   
	   
	   for(int i =0;i<neigh.size();i++)
	   {
		   
		   ArrayList<ArrayList<Integer>> foodarray = new ArrayList<ArrayList<Integer>>();
		   foodarray = getCopy(state.get(1));
		   int x= state.get(0).get(0).get(0);
		   int y= state.get(0).get(0).get(1);
		   int nextX = 0,nextY = 0;
		   if(x+neigh.get(i).getX()>-1 && x+neigh.get(i).getX()<grid.length && y+neigh.get(i).getY()>-1 && y+neigh.get(i).getY()<grid[0].length)
		   {
			   nextX = (int) (x+neigh.get(i).getX());
		   	   nextY = (int) (y+neigh.get(i).getY());
		   	   if(!allWallLocations.contains(new Point(nextX,nextY)))
			   {
				   foodarray.get(nextX).set(nextY, 0);
				   
				   ArrayList<Point> path = new ArrayList<Point>();
				   
				   path.add(new Point(nextX,nextY));
				   ArrayList<ArrayList<Integer>> newPos = new ArrayList<ArrayList<Integer>>();
				   ArrayList<Integer> x1 = new ArrayList<Integer>();
				   x1.add(0, nextX);
				   x1.add(1, nextY);
				   newPos.add(x1);
			
				   Node s = new Node(newPos,foodarray,path,1);
				   allSuccessors.add(s);
			   }
		   }
		   
		   
	   }
	   
	   
	   return allSuccessors;
   }
   
   //below method   implements a star algorithm 
   //it searches all the neighbours and put it to the priority queue and from the queue each neighbour ill be popped out
   //and searched for neighbours having least distance cost +heuristic cost
   public ArrayList<Point> astar (ArrayList<ArrayList<Integer>> cLoc,ArrayList<ArrayList<Integer>> foodSet,PacCell[][] grid,HashSet<Point> allWallLocations) throws IOException
   {
	   ArrayList<Point> path = new ArrayList<Point>();
	   ArrayList<PacFace> direction = new ArrayList<PacFace>();
	   Integer priority = 0;
	   ArrayList<ArrayList<ArrayList<Integer>>> state= new ArrayList<ArrayList<ArrayList<Integer>>>();
	   state.add(cLoc);
	   state.add(foodSet);
	   Node n1 = new Node(cLoc,foodSet,path,priority);
	   PriorityQueue<Node> queue = new PriorityQueue<AstarSearch.Node>(costComparator);
	   queue.add(n1);
	   HashSet<ArrayList<ArrayList<ArrayList<Integer>>>> visited = new HashSet<ArrayList<ArrayList<ArrayList<Integer>>>>();
	   while(!queue.isEmpty())
	   {
		   Node n = queue.poll();
		   if (visited.contains(n.state))
			   	continue;
		   ArrayList<ArrayList<ArrayList<Integer>>> tmp= new ArrayList<ArrayList<ArrayList<Integer>>>();
		   tmp.add(n.state.get(0));tmp.add(n.state.get(1));
		   visited.add(tmp);
		   
		   if (getOneCount(n.state.get(1))==0)
		   {
			   return n.route;
//			   System.out.println("final route size is:---->"+n.route.size());
//			   System.out.println("final direction size is:---->"+n.direction.size());
//			    for(int i  = 0;i<n.route.size();i++)
//			    {
//			    	System.out.print(n.route.get(i).x+" "+n.route.get(i).y+"    ");
//			    }
//			    
//			   	ArrayList<routendir> rnd = new ArrayList<PacSimReplan.routendir>();
//			   				//frame route and direction object and send it to other method
//			   				for(int k = 0;k<n.route.size();k++)
//			   				{
//			   					routendir r1 = new routendir(n.route.get(k), n.direction.get(k));
//			   					rnd.add(r1);
//			   				}
//			   				
//			   				return rnd;
		   }
		   else
		   {
			  ArrayList<Node> succ = new ArrayList<Node>();
			  succ = getSuccessors(n.state,grid,allWallLocations);
			  for(Node s:succ)
			  {
				  if(!visited.contains(s.state))
				  {
					   //add the entire route list
					   ArrayList<Point> newList = new ArrayList<Point>();
					   ArrayList<Point> t1 = new ArrayList<Point>();
					   t1 = n.route;
					   if(t1!=null && !t1.isEmpty() &&t1.size()>0)
					   {
						   newList.addAll(t1);
					   }
					   ArrayList<Point> t3 = new ArrayList<Point>();
					   t3 = s.route;				   				   			   
					   if(!t3.isEmpty() && t3!=null && t3.size()>0)
						   newList.addAll(t3);
					  
					   
					  
					  //add the cumulative priority i.e., sum of priority ie.,()cost of getting  from initial node to n and hueristic cost  estimate of the cost to get from n to any goal node
					  int priority1 = n.priority+s.priority+computetotalHcost(s.state, grid, allWallLocations);
					  //final all state, route and cumulaive priority to the node
					  Node n5 = new Node(s.state.get(0),s.state.get(1),newList,priority1);
					  queue.add(n5);
				  }
			  }
		   }
	   }
	   //bw.close();
	   
	   
	   
	   
	   return null;
	   
   }
   
   @Override
   public PacFace action( Object state ) {

	  counter++;
	  
      PacCell[][] grid = (PacCell[][]) state;
      PacFace newFace = null;
      PacmanCell pc = PacUtils.findPacman( grid );
      
      // make sure Pacman is in this game
      if( pc != null ) {
         PacFace face = pc.getFace();
         //when the counter is called first it calculates entire path and stores inthe array list
         //and also calculates the direction for the solution path and stores in the arraylist
         //Untill all solution path is covered every time when the getAction is called it will send the required direction 
         //command as return variable
         if(counter==1)
         {
        	 //formulate food set
        	 ArrayList<ArrayList<Integer>> foodSet = new ArrayList<ArrayList<Integer>>();
        	 ArrayList<ArrayList<Integer>> pos = new ArrayList<ArrayList<Integer>>();
        	 HashSet<Point> allWallLocations = new HashSet<Point>();
         	 
        	 
        	 //add the pacman intial loc to the pos arraylist
        	 ArrayList<Integer> m = new ArrayList<Integer>();
        	 m.add(pc.getLoc().x);
        	 
        	 
        	 for(int i =0;i<grid.length;i++)
      	   	{
      		   for(int j = 0;j<grid[i].length;j++)
      		   {
      			   if (grid[i][j] instanceof WallCell)
      			   {
      				   allWallLocations.add(new Point(i,j));
      				   //fcount++;
      			   }
      		   }
      	   }
        	 
        	 
        	 for(int i =0;i<grid.length;i++)
        	 {
        		 ArrayList<Integer> temp2 = new ArrayList<Integer>();
        		 for(int j = 0;j<grid[i].length;j++)
        		 {
        			 if(grid[i][j] instanceof FoodCell)
        			 {
        				 temp2.add(j,1);
        			 }
        			 else
        			 {
        				 temp2.add(j,0);
        			 }
        		 }
        		 foodSet.add(temp2);
        	 }
        	 
        	 //set the position to x and y
        	ArrayList<Integer> l1 = new ArrayList<Integer>();
        	l1.add(0,pc.getX());
        	l1.add(1,pc.getY());
        	pos.add(l1);
        	 try {
        		//System.out.println("initial postion of pacman xpos is:"+pos.get(0).get(0));
        		//System.out.println("initial postion of pacman ypos is:"+pos.get(0).get(1));
				path = astar(pos,foodSet,grid,allWallLocations);
				System.out.println("\nNodes expanded :"+nodes);
				Point initialpos = pc.getLoc();
				System.out.println("solution path:");
				for(int i =0;i<path.size();i++)
				{
					System.out.println("("+path.get(i).x+","+path.get(i).y+")");
				}
				for(int i =0;i<path.size();i++)
				{
					   if(path.get(i).x-initialpos.x==1 && path.get(i).y-initialpos.y==0)
					   {
						   direction.add(PacFace.E);
					   }
					   else if(path.get(i).x-initialpos.x==-1 && path.get(i).y-initialpos.y==0)
					   {
						   direction.add(PacFace.W);
					   }
					   else if(path.get(i).x-initialpos.x==0 && path.get(i).y-initialpos.y==-1)
					   {
						   direction.add(PacFace.N);
					   }
					   else if(path.get(i).x-initialpos.x==0 && path.get(i).y-initialpos.y==1)
					   {
						   direction.add(PacFace.S);
					   }
					initialpos = path.get(i);
				}
//				for(int i =0;i<path.size();i++)
//				{
//					System.out.print(direction.get(i)+" ");
//				}
//				System.out.println("Direction size is :\n"+direction.size());
//				System.out.println("Path size is :\n"+path.size());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	 //System.out.println("Total path is");
         }
         if( pc.getLoc().equals(target) ) 
         {
               target = null;
//               System.out.println("Reached target");
//               System.out.println("Pacman Current Face is"+face);
               //System.out.println("Paman current Loc is"+pc.getLoc());
               //System.out.println("target is"+target);
//               System.out.println("path size is"+path.size());
         }
                  
                   
            if( target == null && path.size()>0 ) {
            	target  = path.remove(0);
            
//                  System.out.println("Setting new target: " + target.toString());
            }
            
            if( newFace == null && direction.size()>0 ) {
            	newFace  = direction.remove(0);
//            	System.out.println("New face: " + newFace.name());
               
            }
            if(path.size()==0)
            {
            	counter = 0;
            	nodes = 0;
            }
                        
      }
      return newFace;
   }
}