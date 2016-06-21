package burlap.domain.singleagent.bees;

public class BeesConfig {
	Domain domain;
	Planner planner;
	Learner learner;
	
	class Domain {
		Map map;
		Agent agent;
		Bees bees;
		Honey honey;
		Rewards rewards;
		
		class Map {
			int[] size;
		}
		
		class Agent {
			int health;
			int hunger;
			int[] spawn;
		}
		
		class Bees {
			int count;
			int[] spawn;
		}
		
		class Honey {
			int[] spawn;
		}
		
		class Rewards {
			double goal;
			double lost;
			double sting;
			double honey;
			double defaults;
		}
	}
	
	class Planner {
		String output;
		double gamma;
		double max_delta;
		int max_rollouts;
		int rollout_depth;
		double lower_vinit;
		double upper_vinit;	
		int horizon;
		int exploration;
	}
	
	class Learner {
		String output;
		int episodes;
		double save_percent;
		int post_win_save;
		
		double gamma;
		double qinit;
		double learning_rate;
		int max_steps;
		double epsilon;
	}
}
