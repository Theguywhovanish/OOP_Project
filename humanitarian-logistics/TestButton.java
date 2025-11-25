import com.humanitarian.logistics.ui.Model;
import com.humanitarian.logistics.database.DatabaseLoader;

public class TestButton {
    public static void main(String[] args) {
        System.out.println("Testing DatabaseLoader with curated DB save...");
        try {
            Model model = new Model();
            System.out.println("Before: " + model.getPosts().size() + " posts");
            
            DatabaseLoader.loadOurDatabase(model);
            System.out.println("After: " + model.getPosts().size() + " posts");
            System.out.println("✓ DatabaseLoader works!");
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
