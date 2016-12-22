
import java.awt.Point;
import java.io.File;
import javax.swing.JFileChooser;
import pacsim.FoodCell;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;

/**
 * 
 * @author glinosd
 */
public class PacSimReplan implements PacAction {
   
   private Point target;
      
   public PacSimReplan( String fname ) {
      PacSim sim = new PacSim( fname );
      sim.init(this);
   }
   
   public static void main( String[] args ) {   
      
      //new PacSimReplan( args[ 0 ] );
      
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
      new PacSimReplan( fname );
   }

   @Override
   public void init() {
      target = null;
   }
   
   @Override
   public PacFace action( Object state ) {

      PacCell[][] grid = (PacCell[][]) state;
      PacFace newFace = null;
      PacmanCell pc = PacUtils.findPacman( grid );
      
      // make sure Pacman is in this game
      if( pc != null ) {
         PacFace face = pc.getFace();
         
         if( pc.getLoc().equals(target) ) {
               target = null;
               System.out.println("Reached target");
         }
                  
         // if next cell in current direction is food, then keep going
         if( PacUtils.neighbor(face, pc, grid) instanceof FoodCell ) {
            newFace = face;
         }

         // otherwise, look for food in an adjacent cell
         else if ( PacUtils.neighbor( PacFace.N , pc, grid) instanceof FoodCell) {
            newFace = PacFace.N;
         }
         else if ( PacUtils.neighbor( PacFace.W , pc, grid) instanceof FoodCell) {
            newFace = PacFace.W;
         }
         else if ( PacUtils.neighbor( PacFace.S , pc, grid) instanceof FoodCell) {
            newFace = PacFace.S;
         }
         else if ( PacUtils.neighbor( PacFace.E , pc, grid) instanceof FoodCell) {
            newFace = PacFace.E;
         }
         
         // otherwise, head for the nearest goody
         else {
            if( target == null ) {
               target = PacUtils.nearestGoody( pc.getLoc(), grid);
                  System.out.println("Setting new target: " + target.toString());
               }
            
            newFace = PacUtils.euclideanShortestToTarget(
                  pc.getLoc(), face, target, grid);
            if( newFace == null ) {
               newFace = PacUtils.reverse(face);
            }
            System.out.println("New face: " + newFace.name());            
         }
      }
      return newFace;
   }
}
