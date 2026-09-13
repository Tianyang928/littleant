package net.tianyang928.littleant.entity.ai.brain;

import net.tianyang928.littleant.LittleAnt;

import java.util.*;

/** Single source of truth for toolbox, validation and runtime module metadata. */
public final class ModuleRegistry {
    private static final Map<String, Integer> CATEGORY_COLORS = new LinkedHashMap<>();
    static {
        CATEGORY_COLORS.put("event", 0xFFBF00FF );
        CATEGORY_COLORS.put("behavior", 0xFF4C97FF );
        CATEGORY_COLORS.put("control", 0xFFFFAB19 );
        CATEGORY_COLORS.put("operator", 0xFF59C059 );
        CATEGORY_COLORS.put("goal", 0xFF9966FF );
        CATEGORY_COLORS.put("sense", 0xFF5CB1D6 );
        CATEGORY_COLORS.put("variables", 0xFFFF8C1A );
    }
    private static final Map<String, BlockDefinition> MODULES = new LinkedHashMap<>();
    static {
        // event
        add("tick_start", "event", BlockShape.HAT, List.of(),List.of("tick_start"));
        add("ai_start", "event", BlockShape.HAT, List.of(),List.of("ai_start"));
        add("submit_foreground_goal", "event", BlockShape.COMMAND, List.of(new InputDefinition("goal", ValueType.TEXT, "")),List.of("submit_foreground_goal", "()"));
        add("submit_background_goal", "event", BlockShape.COMMAND, List.of(new InputDefinition("goal", ValueType.TEXT, ""),new InputDefinition("priority", ValueType.NUMBER, "1"),new InputDefinition("move_flag", ValueType.BOOLEAN, ""),new InputDefinition("look_flag", ValueType.BOOLEAN, ""),new InputDefinition("jump_flag", ValueType.BOOLEAN, "")),List.of("submit_background_goal", "()", "priority", "()", "flags", "move_flag", "<>", "look_flag", "<>", "jump_flag", "<>"));
        add("receive_goal", "event", BlockShape.HAT, List.of(new InputDefinition("goal", ValueType.TEXT, "custom_goal")),List.of("receive_goal","()"));
        add("goal_tick_start", "event", BlockShape.HAT, List.of(new InputDefinition("goal", ValueType.TEXT, "custom_goal")),List.of("goal_tick_start","()"));
        add("function_start", "event", BlockShape.HAT, List.of(new InputDefinition("name", ValueType.TEXT, "function")),List.of("function_start","()"));
        add("call_function", "event", BlockShape.COMMAND, List.of(new InputDefinition("name", ValueType.TEXT, "function")),List.of("call_function","()"));
        add("finish_current_goal", "event", BlockShape.COMMAND, List.of(),List.of("finish_current_goal"));
        add("finish_current_goal_delay", "event", BlockShape.COMMAND, List.of(),List.of("finish_current_goal_delay"));

        // behavior
        add("move_to_xyz", "behavior", BlockShape.COMMAND, blockPos("x", "y", "z"),List.of("move_to_xyz","x","()","y","()","z","()"));
        add("move_to_blockpos", "behavior", BlockShape.COMMAND, List.of(new InputDefinition("blockpos", ValueType.LIST, "")),List.of("move_to_blockpos","()"));
        add("step_forward", "behavior", BlockShape.COMMAND, List.of(new InputDefinition("distance", ValueType.NUMBER, "1")),List.of("step_forward","()"));
        add("look_at_xyz", "behavior", BlockShape.COMMAND, blockPos("x", "y", "z"),List.of("look_at_xyz","x","()","y","()","z","()"));
        add("look_at_blockpos", "behavior", BlockShape.COMMAND, List.of(new InputDefinition("blockpos", ValueType.LIST, "")),List.of("look_at_blockpos","()"));
        //TODO: rotate (can't rotate a specific direction)
        //add("rotate", "behavior", BlockShape.COMMAND, List.of(new InputDefinition("angle", ValueType.NUMBER, "45")),List.of("rotate","()"));
        add("say", "behavior", BlockShape.COMMAND, List.of(new InputDefinition("message", ValueType.TEXT, "")),List.of("say","()"));
        add("switch_inventory_slot","behavior",BlockShape.COMMAND, List.of(new InputDefinition("slot", ValueType.NUMBER, "0")),List.of("switch_inventory_slot","()"));
        add("drop_selected_item", "behavior", BlockShape.COMMAND, List.of(), List.of("drop_selected_item"));
        add("jump", "behavior", BlockShape.COMMAND, List.of(),List.of("jump"));
        add("set_run", "behavior", BlockShape.COMMAND, List.of(new InputDefinition("run", ValueType.BOOLEAN, "")),List.of("set_run","<>"));
        add("set_crouching", "behavior", BlockShape.COMMAND, List.of(new InputDefinition("crouching", ValueType.BOOLEAN, "")),List.of("set_crouching","<>"));
        add("set_pheromone", "behavior", BlockShape.COMMAND, List.of(new InputDefinition("pheromone", ValueType.TEXT, "")),List.of("set_pheromone","()"));

        // control
        add("repeat", "control", BlockShape.C_SHAPE, List.of(new InputDefinition("count", ValueType.NUMBER, "10", true), new InputDefinition("body", ValueType.BLOCK, "")),List.of("repeat","times","()","{}"));
        add("if", "control", BlockShape.C_SHAPE, List.of(new InputDefinition("condition", ValueType.BOOLEAN, "", true), new InputDefinition("body", ValueType.BLOCK, "")),List.of("if","<>","{}"));
        add("if_else", "control", BlockShape.E_SHAPE, List.of(new InputDefinition("condition", ValueType.BOOLEAN, "", true), new InputDefinition("body_if", ValueType.BLOCK, ""),new InputDefinition("body_else", ValueType.BLOCK, "")),List.of("if","<>","{}","else","{}"));
        add("while", "control", BlockShape.C_SHAPE, List.of(new InputDefinition("condition", ValueType.BOOLEAN, "", true), new InputDefinition("body", ValueType.BLOCK, "")),List.of("while","<>","{}"));
        add("break", "control", BlockShape.COMMAND, List.of(), List.of("break"));
        add("continue", "control", BlockShape.COMMAND, List.of(), List.of("continue"));

        // operator
        add("add", "operator", BlockShape.REPORTER, numbers("a", "b"),List.of("()","add","()"));
        add("subtract", "operator", BlockShape.REPORTER, numbers("a", "b"),List.of("()","subtract","()"));
        add("multiply", "operator", BlockShape.REPORTER, numbers("a", "b"),List.of("()","multiply","()"));
        add("divide", "operator", BlockShape.REPORTER, numbers("a", "b"),List.of("()","divide","()"));
        add("mod", "operator", BlockShape.REPORTER, numbers("a", "b"),List.of("()","mod","()"));
        add("absolute", "operator", BlockShape.REPORTER, List.of(new InputDefinition("number", ValueType.NUMBER, "0", true)),List.of("absolute","()"));
        add("random", "operator", BlockShape.REPORTER, numbers("min", "max"),List.of("random","min","()","max","()"));
        add("greater_than", "operator", BlockShape.BOOLEAN, numbers("a", "b"),List.of("()","greater_than","()"));
        add("greater_than_or_equal", "operator", BlockShape.BOOLEAN, numbers("a", "b"),List.of("()","greater_than_or_equal","()"));
        add("less_than_or_equal", "operator", BlockShape.BOOLEAN, numbers("a", "b"),List.of("()","less_than_or_equal","()"));
        add("less_than", "operator", BlockShape.BOOLEAN, numbers("a", "b"),List.of("()","less_than","()"));
        add("equal", "operator", BlockShape.BOOLEAN, numbers("a", "b"),List.of("()","equal","()"));
        add("not", "operator", BlockShape.BOOLEAN, List.of(new InputDefinition("condition", ValueType.BOOLEAN, "", true)),List.of("not","<>"));
        add("and", "operator", BlockShape.BOOLEAN, List.of(new InputDefinition("condition_a", ValueType.BOOLEAN, "", true), new InputDefinition("condition_b", ValueType.BOOLEAN, "", true)),List.of("<>","and","<>"));
        add("or", "operator", BlockShape.BOOLEAN, List.of(new InputDefinition("condition_a", ValueType.BOOLEAN, "", true), new InputDefinition("condition_b", ValueType.BOOLEAN, "", true)),List.of("<>","or","<>"));
        add("true", "operator", BlockShape.BOOLEAN, List.of(),List.of("true"));
        add("false", "operator", BlockShape.BOOLEAN, List.of(),List.of("false"));
        add("join_string_list", "operator", BlockShape.REPORTER, List.of(new InputDefinition("strings", ValueType.LIST, "")),List.of("join_string","()"));
        add("join_string_str", "operator", BlockShape.REPORTER, List.of(new InputDefinition("string1", ValueType.TEXT, ""), new InputDefinition("string2", ValueType.TEXT, "")),List.of("join_string","()","and","()"));
        add("contain_str", "operator", BlockShape.BOOLEAN, List.of(new InputDefinition("source", ValueType.TEXT, ""),new InputDefinition("target", ValueType.TEXT, "")),List.of("()","contain_str","()"));

        // goal
        add("break_block_xyz", "goal", BlockShape.REPORTER, blockPos("x", "y", "z"),List.of("break_block_xyz","x","()","y","()","z","()"));
        add("break_block_blockpos", "goal", BlockShape.REPORTER, List.of(new InputDefinition("blockpos", ValueType.LIST, "")),List.of("break_block_blockpos","()"));
        add("set_block_xyz", "goal", BlockShape.REPORTER, blockPos("x", "y", "z"),List.of("set_block_xyz","x","()","y","()","z","()"));
        add("set_block_blockpos", "goal", BlockShape.REPORTER, List.of(new InputDefinition("blockpos", ValueType.LIST, "")),List.of("set_block_blockpos","()"));
        add("use_crafting_table_xyz", "goal", BlockShape.REPORTER, craftItem(9, true, false),List.of("use_crafting_table","amount","()","pos","()","()","()","3x3"));
        add("use_crafting_table_blockpos", "goal", BlockShape.REPORTER, craftItem(9, true, true),List.of("use_crafting_table","amount","()","pos","()","3x3"));
        add("use_inventory_crafting", "goal", BlockShape.REPORTER, craftItem(4, false, false),List.of("use_inventory_crafting","amount","()","2x2"));
        add("better_float", "goal", BlockShape.REPORTER, List.of(),List.of("better_float"));
        add("use_container_xyz", "goal", BlockShape.REPORTER, List.of(new InputDefinition("x", ValueType.NUMBER, "0", true), new InputDefinition("y", ValueType.NUMBER, "0", true), new InputDefinition("z", ValueType.NUMBER, "0", true), new InputDefinition("put_in", ValueType.BOOLEAN, "", true), new InputDefinition("item", ValueType.TEXT, "minecraft:stone", true), new InputDefinition("slot", ValueType.NUMBER, "0", true), new InputDefinition("amount", ValueType.NUMBER, "1", true)),List.of("use_container","pos","()","()","()","put_in","<>","item","()","slot","()","amount","()"));
        add("use_container_blockpos", "goal", BlockShape.REPORTER, List.of(new InputDefinition("blockpos", ValueType.LIST, "", true), new InputDefinition("put_in", ValueType.BOOLEAN, "", true), new InputDefinition("item", ValueType.TEXT, "minecraft:stone", true), new InputDefinition("slot", ValueType.NUMBER, "0", true), new InputDefinition("amount", ValueType.NUMBER, "1", true)),List.of("use_container","pos","()","put_in","<>","item","()","slot","()","amount","()"));
        add("melee_attack", "goal", BlockShape.REPORTER, List.of(new InputDefinition("target", ValueType.NUMBER, "", true)),List.of("melee_attack","target","()"));
        add("clear_goal", "goal", BlockShape.COMMAND, List.of(),List.of("clear_goal"));
        add("already_has_goal", "goal", BlockShape.BOOLEAN, List.of(new InputDefinition("goal", ValueType.TEXT, "")),List.of("already_has_goal","()"));
        add("already_has_goal_at_priority", "goal", BlockShape.BOOLEAN, List.of(new InputDefinition("goal", ValueType.TEXT, ""), new InputDefinition("priority", ValueType.NUMBER, "1", true)),List.of("already_has_goal","()","priority","()"));
        add("use_item", "goal", BlockShape.REPORTER, List.of(), List.of("use_item"));
        add("use_block_xyz", "goal", BlockShape.REPORTER,
                List.of(new InputDefinition("x", ValueType.NUMBER, "0", true), new InputDefinition("y", ValueType.NUMBER, "0", true), new InputDefinition("z", ValueType.NUMBER, "0", true),
                        new InputDefinition("face", ValueType.TEXT, "up"), new InputDefinition("held_item", ValueType.BOOLEAN, "")),
                List.of("use_block", "pos", "()", "()", "()", "face", "()", "held_item", "<>"));
        add("use_block_blockpos", "goal", BlockShape.REPORTER,
                List.of(new InputDefinition("blockpos", ValueType.LIST, ""), new InputDefinition("face", ValueType.TEXT, "up"),
                        new InputDefinition("held_item", ValueType.BOOLEAN, "")),
                List.of("use_block", "pos", "()", "face", "()", "held_item", "<>"));
        add("interact_entity", "goal", BlockShape.REPORTER,
                List.of(new InputDefinition("target", ValueType.NUMBER, "-1", true),
                        new InputDefinition("held_item", ValueType.BOOLEAN, "", true)),
                List.of("interact_entity", "target", "()", "held_item", "<>"));

        // sense
        add("health", "sense", BlockShape.REPORTER, List.of(),List.of("health"));
        add("food_level", "sense", BlockShape.REPORTER, List.of(),List.of("food_level"));
        add("x","sense", BlockShape.REPORTER, List.of(),List.of("x"));
        add("y","sense", BlockShape.REPORTER, List.of(),List.of("y"));
        add("z","sense", BlockShape.REPORTER, List.of(),List.of("z"));
        add("pos","sense", BlockShape.REPORTER, List.of(),List.of("pos"));
        add("distance_to_xyz", "sense", BlockShape.REPORTER, blockPos("x","y","z"),List.of("distance_to_xyz","x","()","y","()","z","()"));
        add("distance_to_blockpos", "sense", BlockShape.REPORTER, List.of(new InputDefinition("blockpos", ValueType.LIST, "")),List.of("distance_to_blockpos","()"));
        add("get_block_xyz","sense", BlockShape.REPORTER, blockPos("x","y","z"),List.of("get_block_xyz","x","()","y","()","z","()"));
        add("get_block_blockpos", "sense", BlockShape.REPORTER, List.of(new InputDefinition("blockpos", ValueType.LIST, "")),List.of("get_block_blockpos","()"));
        add("get_entity_pos","sense", BlockShape.REPORTER, List.of(new InputDefinition("id", ValueType.NUMBER, "")),List.of("get_entity_pos","()"));
        add("has_item_in_inventory","sense", BlockShape.BOOLEAN, List.of(new InputDefinition("item", ValueType.TEXT, "minecraft:stone")),List.of("has_item_in_inventory","()"));
        add("get_item_in_inventory","sense", BlockShape.REPORTER, List.of(new InputDefinition("slot", ValueType.NUMBER, "0", true)),List.of("get_item_in_inventory","()"));
        add("get_item_count_in_inventory","sense", BlockShape.REPORTER, List.of(new InputDefinition("slot", ValueType.NUMBER, "0", true)),List.of("get_item_count_in_inventory","()"));
        add("get_selected_slot", "sense", BlockShape.REPORTER, List.of(), List.of("get_selected_slot"));
        add("time","sense", BlockShape.REPORTER, List.of(),List.of("time"));
        add("is_hurt","sense", BlockShape.BOOLEAN, List.of(),List.of("is_hurt"));
        add("is_on_fire","sense", BlockShape.BOOLEAN, List.of(),List.of("is_on_fire"));
        add("is_in_water","sense", BlockShape.BOOLEAN, List.of(),List.of("is_in_water"));
        add("is_under_water","sense", BlockShape.BOOLEAN, List.of(),List.of("is_under_water"));
        add("last_hurt_by_entity","sense", BlockShape.REPORTER, List.of(),List.of("last_hurt_by_entity"));
        add("find_nearest_block", "sense", BlockShape.REPORTER, List.of(new InputDefinition("block", ValueType.TEXT, "minecraft:stone")),List.of("find_nearest_block","()"));
        add("find_nearest_entity", "sense", BlockShape.REPORTER, List.of(new InputDefinition("entity", ValueType.TEXT, "minecraft:pig")),List.of("find_nearest_entity","()"));
        add("find_nearest_block_entity", "sense", BlockShape.REPORTER, List.of(new InputDefinition("block_entity", ValueType.TEXT, "minecraft:chest")),List.of("find_nearest_block_entity","()"));
        add("find_nearest_pheromone", "sense", BlockShape.REPORTER, List.of(new InputDefinition("pheromone", ValueType.TEXT, "home")),List.of("find_nearest_pheromone","()"));
        add("find_nearest_drop","sense", BlockShape.REPORTER, List.of(new InputDefinition("drop", ValueType.TEXT, "minecraft:stone")),List.of("find_nearest_drop","()"));
        add("find_block_list", "sense", BlockShape.REPORTER, List.of(new InputDefinition("block", ValueType.TEXT, "minecraft:stone"),new InputDefinition("count", ValueType.NUMBER, "256")),List.of("find_block_list","()","count","()"));
        add("find_entity_list", "sense", BlockShape.REPORTER, List.of(new InputDefinition("entity", ValueType.TEXT, "minecraft:pig"),new InputDefinition("count", ValueType.NUMBER, "256")),List.of("find_entity_list","()","count","()"));
        add("find_block_entity_list", "sense", BlockShape.REPORTER, List.of(new InputDefinition("block_entity", ValueType.TEXT, "minecraft:chest"),new InputDefinition("count", ValueType.NUMBER, "256")),List.of("find_block_entity_list","()","count","()"));
        add("find_pheromone_list", "sense", BlockShape.REPORTER, List.of(new InputDefinition("pheromone", ValueType.TEXT, "home"),new InputDefinition("count", ValueType.NUMBER, "256")),List.of("find_pheromone_list","()","count","()"));
        add("find_drop_list","sense", BlockShape.REPORTER, List.of(new InputDefinition("drop", ValueType.TEXT, "minecraft:stone"),new InputDefinition("count", ValueType.NUMBER, "256")),List.of("find_drop_list","()","count","()"));
        add("get_surrounding_pheromone_types", "sense", BlockShape.REPORTER, List.of(),List.of("get_surrounding_pheromone_types"));
        add("has_item_in_container_xyz", "sense", BlockShape.BOOLEAN, List.of(new InputDefinition("x", ValueType.NUMBER, "0", true), new InputDefinition("y", ValueType.NUMBER, "0", true), new InputDefinition("z", ValueType.NUMBER, "0", true), new InputDefinition("item", ValueType.TEXT, "minecraft:stone")),List.of("has_item_in_container","x","()","y","()","z","()","item","()"));
        add("has_item_in_container_blockpos", "sense", BlockShape.BOOLEAN, List.of(new InputDefinition("blockpos", ValueType.LIST, ""), new InputDefinition("item", ValueType.TEXT, "minecraft:stone")),List.of("has_item_in_container","pos","()","item","()"));
        add("get_item_in_container_xyz", "sense", BlockShape.REPORTER, List.of(new InputDefinition("x", ValueType.NUMBER, "0", true), new InputDefinition("y", ValueType.NUMBER, "0", true), new InputDefinition("z", ValueType.NUMBER, "0", true),new InputDefinition("slot", ValueType.NUMBER, "0", true)),List.of("get_item_in_container","x","()","y","()","z","()","slot","()"));
        add("get_item_in_container_blockpos", "sense", BlockShape.REPORTER, List.of(new InputDefinition("blockpos", ValueType.LIST, ""),new InputDefinition("slot", ValueType.NUMBER, "0", true)),List.of("get_item_in_container","pos","()","slot","()"));
        add("get_item_count_in_container_xyz", "sense", BlockShape.REPORTER, List.of(new InputDefinition("x", ValueType.NUMBER, "0", true), new InputDefinition("y", ValueType.NUMBER, "0", true), new InputDefinition("z", ValueType.NUMBER, "0", true),new InputDefinition("slot", ValueType.NUMBER, "0", true)),List.of("get_item_count_in_container","x","()","y","()","z","()","slot","()"));
        add("get_item_count_in_container_blockpos", "sense", BlockShape.REPORTER, List.of(new InputDefinition("blockpos", ValueType.LIST, ""),new InputDefinition("slot", ValueType.NUMBER, "0", true)),List.of("get_item_count_in_container","pos","()","slot","()"));
        add("get_speed", "sense", BlockShape.REPORTER, List.of(),List.of("get_speed"));
        add("is_running", "sense", BlockShape.BOOLEAN, List.of(),List.of("is_running"));
        add("is_crouching", "sense", BlockShape.BOOLEAN, List.of(),List.of("is_crouching"));
        add("is_in_tag", "sense", BlockShape.BOOLEAN, List.of(new InputDefinition("target", ValueType.TEXT, ""),new InputDefinition("tag", ValueType.TEXT, "")),List.of("()","is_in_tag","()"));

        // variables
        add("set_variable", "variables", BlockShape.COMMAND, List.of(new InputDefinition("name", ValueType.TEXT, ""), new InputDefinition("value", ValueType.NUMBER, "0")),List.of("set_variable","()","value","()"));
        add("get_variable", "variables", BlockShape.REPORTER, List.of(new InputDefinition("name", ValueType.TEXT, "")),List.of("get_variable","()"));
        add("set_variable_permanent", "variables", BlockShape.COMMAND, List.of(new InputDefinition("name", ValueType.TEXT, "")),List.of("set_variable_permanent","()"));
        add("new_list", "variables", BlockShape.COMMAND, List.of(new InputDefinition("name", ValueType.TEXT, "")),List.of("new_list","()"));
        add("get_list", "variables", BlockShape.REPORTER, List.of(new InputDefinition("name", ValueType.TEXT, "")),List.of("get_list","()"));
        add("add_list", "variables", BlockShape.COMMAND, List.of(new InputDefinition("name", ValueType.TEXT, ""),new InputDefinition("list", ValueType.LIST, "")),List.of("name","()","add_list","()"));
        add("add_value", "variables", BlockShape.COMMAND, List.of(new InputDefinition("name", ValueType.TEXT, ""),new InputDefinition("value", ValueType.TEXT, "")),List.of("name","()","add_value","()"));
        add("set_list_kv", "variables", BlockShape.COMMAND, List.of(new InputDefinition("name", ValueType.TEXT, ""), new InputDefinition("key", ValueType.NUMBER, ""), new InputDefinition("value", ValueType.TEXT, "")),List.of("set_list","()","key","()","value","()"));
        add("set_list_list", "variables", BlockShape.COMMAND, List.of(new InputDefinition("name", ValueType.TEXT, ""), new InputDefinition("list", ValueType.LIST, "")),List.of("set_list","()","list","()"));
        add("get_list_value", "variables", BlockShape.REPORTER, List.of(new InputDefinition("name", ValueType.TEXT, ""), new InputDefinition("key", ValueType.NUMBER, "")),List.of("get_list_value","()","key","()"));
        add("set_list_permanent", "variables", BlockShape.COMMAND, List.of(new InputDefinition("name", ValueType.TEXT, "")),List.of("set_list_permanent","()"));
        add("clear_list", "variables", BlockShape.COMMAND, List.of(new InputDefinition("name", ValueType.TEXT, "")),List.of("clear_list","()"));
    }

    private static List<InputDefinition> numbers(String a, String b) {
        return List.of(new InputDefinition(a, ValueType.NUMBER, "0", true), new InputDefinition(b, ValueType.NUMBER, "0", true));
    }

    private static List<InputDefinition> blockPos(String x, String y, String z) {
        return List.of(new InputDefinition(x, ValueType.NUMBER, "0", true), new InputDefinition(y, ValueType.NUMBER, "0", true), new InputDefinition(z, ValueType.NUMBER, "0", true));
    }

    private static List<InputDefinition> craftItem(int slot, boolean includePosition, boolean isBlockpos) {
        List<InputDefinition> result = new ArrayList<>(List.of(new InputDefinition("amount", ValueType.NUMBER, "1", true)));
        if (includePosition) {
            if(isBlockpos) {
                result.add(new InputDefinition("blockpos", ValueType.LIST, ""));
            }
            else {
                result.addAll(blockPos("x", "y", "z"));
            }
        }
        for(int i = 0; i < slot; i++) {
            result.add(new InputDefinition("slot" + i, ValueType.TEXT, "minecraft:air", true));
        }
        return result;
    }

    private static void add(String op, String cat, BlockShape shape, List<InputDefinition> in, List<String> displayFormat) {
        List<String> polishedFormat = new ArrayList<>();
        for(String displayElement : displayFormat) {
            if(displayElement.equals("()")
                    || displayElement.equals("[]")
                    || displayElement.equals("<>")
                    || displayElement.equals("{}")
                    || displayElement.equals("3x3")
                    || displayElement.equals("2x2")) {
                polishedFormat.add(displayElement);
            }
            else {
                polishedFormat.add("opcode.littleant.ant_brain." + displayElement);
            }
        }
        MODULES.put(op, new BlockDefinition(op, cat, shape, in, "opcode.littleant.ant_brain." + op, CATEGORY_COLORS.get(cat), polishedFormat));
    }

    public static List<String> getDisplayFormat(String opcode){
        BlockDefinition def = get(opcode);
        if(def == null) {
            return List.of();
        }
        return def.displayFormat();
    }

    public static BlockDefinition get(String opcode) {
        return MODULES.get(opcode);
    }

    public static boolean contains(String opcode) {
        return MODULES.containsKey(opcode);
    }

    /** Value produced by reporter/boolean blocks; used by typed input snapping. */
    public static ValueType outputType(String opcode) {
        BlockDefinition definition = get(opcode);
        if (definition == null) return ValueType.ANY;
        if (definition.shape() == BlockShape.BOOLEAN) return ValueType.BOOLEAN;
        if (definition.shape() != BlockShape.REPORTER) return ValueType.BLOCK;
        return switch (opcode) {
            case "health", "food_level", "x", "y", "z", "distance_to_xyz", "distance_to_blockpos", "time", "get_selected_slot",
                    "add", "subtract", "multiply", "divide", "mod", "absolute", "random" -> ValueType.NUMBER;
            case "pos", "find_nearest_block", "find_nearest_block_entity", "find_nearest_pheromone", "find_nearest_drop",
                    "find_block_list", "find_entity_list", "find_block_entity_list", "find_pheromone_list", "find_drop_list",
                    "get_surrounding_pheromone_types" -> ValueType.LIST;
            default -> ValueType.TEXT;
        };
    }

    public static Collection<BlockDefinition> all() {
        return List.copyOf(MODULES.values());
    }

    public static Map<String, Integer> categoryColors() {
        return Map.copyOf(CATEGORY_COLORS);
    }

    public static int categoryColor(String category) {
        return CATEGORY_COLORS.getOrDefault(category, 0xFF6B778D);
    }

    public static String categoryTranslationKey(String category) {
        return "category.littleant.ant_brain." + category;
    }

    public static List<InputSlot> createDefaultInputs(String opcode) {
        BlockDefinition definition = get(opcode);
        if (definition == null) return List.of();
        return definition.inputs().stream()
                .map(input -> InputSlot.literal(input.name(), input.type(), input.defaultValue()))
                .toList();
    }

    public static Map<String, List<BlockDefinition>> byCategory() {
        Map<String, List<BlockDefinition>> out = new LinkedHashMap<>();
        for (BlockDefinition d : MODULES.values())
            out.computeIfAbsent(d.category(), k -> new ArrayList<>()).add(d);
        return out;
    }

    private ModuleRegistry() {
    }
}
