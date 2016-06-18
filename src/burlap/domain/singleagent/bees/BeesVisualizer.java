package burlap.domain.singleagent.bees;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.visualizer.OOStatePainter;
import burlap.visualizer.ObjectPainter;
import burlap.visualizer.StateRenderLayer;
import burlap.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Shawn Squire.
 */
public class BeesVisualizer {

    private BeesVisualizer() {
        // do nothing
    }

	/**
	 * Returns a {@link burlap.visualizer.Visualizer} for {@link burlap.domain.singleagent.blockdude.Bees}.
	 * @param maxx the max x dimensionality of the world
	 * @param maxy the max y dimensionality of the world
	 * @return a {@link burlap.visualizer.Visualizer} for {@link burlap.domain.singleagent.blockdude.Bees}
	 */
	public static Visualizer getVisualizer(int maxx, int maxy){
		Visualizer v = new Visualizer(getStateRenderLayer(maxx, maxy));
		return v;
	}

	/**
	 * Returns a {@link burlap.visualizer.StateRenderLayer} for {@link burlap.domain.singleagent.blockdude.BeesVisualizer}.
	 * @param maxx the max x dimensionality of the world
	 * @param maxy the max y dimensionality of the world
	 * @return a {@link burlap.visualizer.StateRenderLayer} for {@link burlap.domain.singleagent.blockdude.BeesVisualizer}.
	 */
	public static StateRenderLayer getStateRenderLayer(int maxx, int maxy){

		StateRenderLayer srl = new StateRenderLayer();

		OOStatePainter oopainter = new OOStatePainter();
		srl.addStatePainter(oopainter);

		oopainter.addObjectClassPainter(Bees.CLASS_MAP, new BricksPainter(maxx, maxy));
		oopainter.addObjectClassPainter(Bees.CLASS_AGENT, new AgentPainter(maxx, maxy));
		oopainter.addObjectClassPainter(Bees.CLASS_HONEY, new HoneyPainter(maxx, maxy));
		oopainter.addObjectClassPainter(Bees.CLASS_BEE, new BeePainter(maxx, maxy));

		return srl;
	}


	/**
	 * A class for rendering the agent as a blue square
	 */
	public static class AgentPainter implements ObjectPainter {

		public int minx = 0;
		public int miny = 0;

		public int maxx;
		public int maxy;

		/**
		 * Initializes.
		 * @param maxx the max x dimensionality of the world
		 * @param maxy the max y dimensionality of the world
		 */
		public AgentPainter(int maxx, int maxy){
			this.maxx = maxx;
			this.maxy = maxy;
		}

		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.blue);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = (Integer)ob.get(Bees.VAR_X)*width;
			float ry = cHeight - height - (Integer)ob.get(Bees.VAR_Y)*height;

			g2.fill(new Rectangle2D.Float(rx, ry, width, height));

			// Draw the health and hunger
			g2.drawString("Health: " + ob.get(Bees.VAR_HEALTH), 5, 25);
			g2.drawString("Hunger: " + ob.get(Bees.VAR_HUNGER), 5, 45);
		}


	}

	/**
	 * A class for rendering a bee as a red square
	 */
	public static class BeePainter implements ObjectPainter{

		public int minx = 0;
		public int miny = 0;

		public int maxx;
		public int maxy;

		/**
		 * Initializes.
		 * @param maxx the max x dimensionality of the world
		 * @param maxy the max y dimensionality of the world
		 */
		public BeePainter(int maxx, int maxy){
			this.maxx = maxx;
			this.maxy = maxy;
		}


		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.red);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = (Integer)ob.get(Bees.VAR_X)*width;
			float ry = cHeight - height - (Integer)ob.get(Bees.VAR_Y)*height;

			g2.fill(new Rectangle2D.Float(rx, ry, width, height));
			
			g2.drawString("Bees: " + s.objectsOfClass(Bees.CLASS_BEE).size(), 5, 65);
		}
	}


	/**
	 * A class for rendering honey with a purple square
	 */
	public static class HoneyPainter implements ObjectPainter{
		public int minx = 0;
		public int miny = 0;

		public int maxx;
		public int maxy;

		/**
		 * Initializes.
		 * @param maxx the max x dimensionality of the world
		 * @param maxy the max y dimensionality of the world
		 */
		public HoneyPainter(int maxx, int maxy){

			this.maxx = maxx;
			this.maxy = maxy;
		}

		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.magenta);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = (Integer)ob.get(Bees.VAR_X)*width;
			float ry = cHeight - height - (Integer)ob.get(Bees.VAR_Y)*height;

			g2.fill(new Rectangle2D.Float(rx, ry, width, height));

		}
	}


	/**
	 * A class for rendering bricks as black squares. Since all bricks are specified in a single 1D array, rather than
	 * a separate object for each, this class will iterate through the array and paint each brick.
	 */
	public static class BricksPainter implements ObjectPainter{

		public int minx = 0;
		public int miny = 0;

		public int maxx;
		public int maxy;

		/**
		 * Initializes.
		 * @param maxx the max x dimensionality of the world
		 * @param maxy the max y dimensionality of the world
		 */
		public BricksPainter(int maxx, int maxy){
			this.maxx = maxx;
			this.maxy = maxy;
		}


		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {

			g2.setColor(Color.black);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;


			int [][] map = (int[][])ob.get(Bees.VAR_MAP);

			for(int x = 0; x < map.length; x++){
				for(int y = 0; y < map[0].length; y++){
					float rx = x * width;
					float ry = cHeight - height - y * height;

					if(map[x][y] == 1) {
						g2.fill(new Rectangle2D.Float(rx, ry, width, height));
					}
				}
			}

		}
	}

}

