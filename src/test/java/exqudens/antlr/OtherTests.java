package exqudens.antlr;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherTests {

    public static class Entity {

        public Integer id;
        public String name;
        public List<Entity> children;

    }

    @Test
    public void test1() {
        Entity root = new Entity();

        root.id = 1;
        root.name = "root";
        root.children = new ArrayList<>();

        root.children.add(new Entity());
        root.children.add(new Entity());

        root.children.get(0).id = 2;
        root.children.get(0).name = "child0";
        root.children.get(0).children = new ArrayList<>();

        root.children.get(1).id = 3;
        root.children.get(1).name = "child1";
        root.children.get(1).children = new ArrayList<>();

        Map<String, Object> map = toMap(root);

        System.out.println(map);
    }

    private Map<String, Object> toMap(Object object) {
        try {
            Map<String, Object> map = new HashMap<>();
            Entity entity = (Entity) object;
            map.put("id", entity.id);
            map.put("name", entity.name);
            List<Map<String, Object>> children = new ArrayList<>();
            List<Entity> list = entity.children;
            for (Entity o : list) {
                children.add(toMap(o));
                map.put("children", children);
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
