# LittleAnt AI API

Generated from `ModuleRegistry` (the single source of truth). The DSL is restricted and is not a Python interpreter.

## Example

```python
@tick_start
def main():
    say("hello")
```

## Modules

### `tick_start`

Category: `event` -- Shape: `HAT`

Parameters: none

### `ai_start`

Category: `event` -- Shape: `HAT`

Parameters: none

### `submit_foreground_goal`

Category: `event` -- Shape: `COMMAND`

Parameters:
- `goal` (`TEXT`), default ``

### `submit_background_goal`

Category: `event` -- Shape: `COMMAND`

Parameters:
- `goal` (`TEXT`), default ``
- `priority` (`NUMBER`), default `1`
- `move_flag` (`BOOLEAN`), default ``
- `look_flag` (`BOOLEAN`), default ``
- `jump_flag` (`BOOLEAN`), default ``

### `receive_goal`

Category: `event` -- Shape: `HAT`

Parameters:
- `goal` (`TEXT`), default `custom_goal`

### `goal_tick_start`

Category: `event` -- Shape: `HAT`

Parameters:
- `goal` (`TEXT`), default `custom_goal`

### `function_start`

Category: `event` -- Shape: `HAT`

Parameters:
- `name` (`TEXT`), default `function`

### `call_function`

Category: `event` -- Shape: `COMMAND`

Parameters:
- `name` (`TEXT`), default `function`

### `finish_current_goal`

Category: `event` -- Shape: `COMMAND`

Parameters: none

### `finish_current_goal_delay`

Category: `event` -- Shape: `COMMAND`

Parameters: none

### `move_to_xyz`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required

### `move_to_blockpos`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `blockpos` (`LIST`), default ``

### `step_forward`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `distance` (`NUMBER`), default `1`

### `look_at_xyz`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required

### `look_at_blockpos`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `blockpos` (`LIST`), default ``

### `rotate`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `angle` (`NUMBER`), default `45`

### `say`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `message` (`TEXT`), default ``

### `switch_inventory_slot`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `slot` (`NUMBER`), default `0`

### `drop_selected_item`

Category: `behavior` -- Shape: `COMMAND`

Parameters: none

### `jump`

Category: `behavior` -- Shape: `COMMAND`

Parameters: none

### `set_run`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `run` (`BOOLEAN`), default ``

### `set_crouching`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `crouching` (`BOOLEAN`), default ``

### `set_pheromone`

Category: `behavior` -- Shape: `COMMAND`

Parameters:
- `pheromone` (`TEXT`), default ``

### `repeat`

Category: `control` -- Shape: `C_SHAPE`

Parameters:
- `count` (`NUMBER`), default `10`; required
- `body` (`BLOCK`), default ``

### `if`

Category: `control` -- Shape: `C_SHAPE`

Parameters:
- `condition` (`BOOLEAN`), default ``; required
- `body` (`BLOCK`), default ``

### `if_else`

Category: `control` -- Shape: `E_SHAPE`

Parameters:
- `condition` (`BOOLEAN`), default ``; required
- `body_if` (`BLOCK`), default ``
- `body_else` (`BLOCK`), default ``

### `while`

Category: `control` -- Shape: `C_SHAPE`

Parameters:
- `condition` (`BOOLEAN`), default ``; required
- `body` (`BLOCK`), default ``

### `break`

Category: `control` -- Shape: `COMMAND`

Parameters: none

### `continue`

Category: `control` -- Shape: `COMMAND`

Parameters: none

### `add`

Category: `operator` -- Shape: `REPORTER`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `subtract`

Category: `operator` -- Shape: `REPORTER`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `multiply`

Category: `operator` -- Shape: `REPORTER`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `divide`

Category: `operator` -- Shape: `REPORTER`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `mod`

Category: `operator` -- Shape: `REPORTER`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `absolute`

Category: `operator` -- Shape: `REPORTER`

Parameters:
- `number` (`NUMBER`), default `0`; required

### `random`

Category: `operator` -- Shape: `REPORTER`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `greater_than`

Category: `operator` -- Shape: `BOOLEAN`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `greater_than_or_equal`

Category: `operator` -- Shape: `BOOLEAN`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `less_than_or_equal`

Category: `operator` -- Shape: `BOOLEAN`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `less_than`

Category: `operator` -- Shape: `BOOLEAN`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `equal`

Category: `operator` -- Shape: `BOOLEAN`

Parameters:
- `a` (`NUMBER`), default `0`; required
- `b` (`NUMBER`), default `0`; required

### `not`

Category: `operator` -- Shape: `BOOLEAN`

Parameters:
- `condition` (`BOOLEAN`), default ``; required

### `and`

Category: `operator` -- Shape: `BOOLEAN`

Parameters:
- `condition_a` (`BOOLEAN`), default ``; required
- `condition_b` (`BOOLEAN`), default ``; required

### `or`

Category: `operator` -- Shape: `BOOLEAN`

Parameters:
- `condition_a` (`BOOLEAN`), default ``; required
- `condition_b` (`BOOLEAN`), default ``; required

### `true`

Category: `operator` -- Shape: `BOOLEAN`

Parameters: none

### `false`

Category: `operator` -- Shape: `BOOLEAN`

Parameters: none

### `join_string_list`

Category: `operator` -- Shape: `REPORTER`

Parameters:
- `strings` (`LIST`), default ``

### `join_string_str`

Category: `operator` -- Shape: `REPORTER`

Parameters:
- `string1` (`TEXT`), default ``
- `string2` (`TEXT`), default ``

### `contain_str`

Category: `operator` -- Shape: `BOOLEAN`

Parameters:
- `source` (`TEXT`), default ``
- `target` (`TEXT`), default ``

### `break_block_xyz`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required

### `break_block_blockpos`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `blockpos` (`LIST`), default ``

### `set_block_xyz`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required

### `set_block_blockpos`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `blockpos` (`LIST`), default ``

### `use_crafting_table_xyz`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `amount` (`NUMBER`), default `1`; required
- `recipe_slots` (`TEXT`), default `minecraft:air`; required

### `use_crafting_table_blockpos`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `amount` (`NUMBER`), default `1`; required
- `recipe_slots` (`TEXT`), default `minecraft:air`; required

### `use_inventory_crafting`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `amount` (`NUMBER`), default `1`; required
- `recipe_slots` (`TEXT`), default `minecraft:air`; required

### `better_float`

Category: `goal` -- Shape: `REPORTER`

Parameters: none

### `use_container_xyz`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required
- `put_in` (`BOOLEAN`), default ``; required
- `item` (`TEXT`), default `minecraft:stone`; required
- `slot` (`NUMBER`), default `0`; required
- `amount` (`NUMBER`), default `1`; required

### `use_container_blockpos`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `blockpos` (`LIST`), default ``; required
- `put_in` (`BOOLEAN`), default ``; required
- `item` (`TEXT`), default `minecraft:stone`; required
- `slot` (`NUMBER`), default `0`; required
- `amount` (`NUMBER`), default `1`; required

### `melee_attack`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `target` (`NUMBER`), default ``; required

### `clear_goal`

Category: `goal` -- Shape: `COMMAND`

Parameters: none

### `already_has_goal`

Category: `goal` -- Shape: `BOOLEAN`

Parameters:
- `goal` (`TEXT`), default ``

### `already_has_goal_at_priority`

Category: `goal` -- Shape: `BOOLEAN`

Parameters:
- `goal` (`TEXT`), default ``
- `priority` (`NUMBER`), default `1`; required

### `use_item`

Category: `goal` -- Shape: `REPORTER`

Parameters: none

### `use_block_xyz`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required
- `face` (`TEXT`), default `up`
- `held_item` (`BOOLEAN`), default ``

### `use_block_blockpos`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `blockpos` (`LIST`), default ``
- `face` (`TEXT`), default `up`
- `held_item` (`BOOLEAN`), default ``

### `interact_entity`

Category: `goal` -- Shape: `REPORTER`

Parameters:
- `target` (`NUMBER`), default `-1`; required
- `held_item` (`BOOLEAN`), default ``; required

### `health`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `food_level`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `x`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `y`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `z`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `pos`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `distance_to_xyz`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required

### `distance_to_blockpos`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `blockpos` (`LIST`), default ``

### `get_block_xyz`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required

### `get_block_blockpos`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `blockpos` (`LIST`), default ``

### `get_entity_pos`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `id` (`NUMBER`), default ``

### `has_item_in_inventory`

Category: `sense` -- Shape: `BOOLEAN`

Parameters:
- `item` (`TEXT`), default `minecraft:stone`

### `get_item_in_inventory`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `slot` (`NUMBER`), default `0`; required

### `get_item_count_in_inventory`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `slot` (`NUMBER`), default `0`; required

### `get_selected_slot`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `time`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `is_hurt`

Category: `sense` -- Shape: `BOOLEAN`

Parameters: none

### `is_on_fire`

Category: `sense` -- Shape: `BOOLEAN`

Parameters: none

### `is_in_water`

Category: `sense` -- Shape: `BOOLEAN`

Parameters: none

### `is_under_water`

Category: `sense` -- Shape: `BOOLEAN`

Parameters: none

### `last_hurt_by_entity`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `find_nearest_block`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `block` (`TEXT`), default `minecraft:stone`

### `find_nearest_entity`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `entity` (`TEXT`), default `minecraft:pig`

### `find_nearest_block_entity`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `block_entity` (`TEXT`), default `minecraft:chest`

### `find_nearest_pheromone`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `pheromone` (`TEXT`), default `home`

### `find_nearest_drop`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `drop` (`TEXT`), default `minecraft:stone`

### `find_block_list`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `block` (`TEXT`), default `minecraft:stone`
- `count` (`NUMBER`), default `256`

### `find_entity_list`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `entity` (`TEXT`), default `minecraft:pig`
- `count` (`NUMBER`), default `256`

### `find_block_entity_list`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `block_entity` (`TEXT`), default `minecraft:chest`
- `count` (`NUMBER`), default `256`

### `find_pheromone_list`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `pheromone` (`TEXT`), default `home`
- `count` (`NUMBER`), default `256`

### `find_drop_list`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `drop` (`TEXT`), default `minecraft:stone`
- `count` (`NUMBER`), default `256`

### `get_surrounding_pheromone_types`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `has_item_in_container_xyz`

Category: `sense` -- Shape: `BOOLEAN`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required
- `item` (`TEXT`), default `minecraft:stone`

### `has_item_in_container_blockpos`

Category: `sense` -- Shape: `BOOLEAN`

Parameters:
- `blockpos` (`LIST`), default ``
- `item` (`TEXT`), default `minecraft:stone`

### `get_item_in_container_xyz`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required
- `slot` (`NUMBER`), default `0`; required

### `get_item_in_container_blockpos`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `blockpos` (`LIST`), default ``
- `slot` (`NUMBER`), default `0`; required

### `get_item_count_in_container_xyz`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `x` (`NUMBER`), default `0`; required
- `y` (`NUMBER`), default `0`; required
- `z` (`NUMBER`), default `0`; required
- `slot` (`NUMBER`), default `0`; required

### `get_item_count_in_container_blockpos`

Category: `sense` -- Shape: `REPORTER`

Parameters:
- `blockpos` (`LIST`), default ``
- `slot` (`NUMBER`), default `0`; required

### `get_speed`

Category: `sense` -- Shape: `REPORTER`

Parameters: none

### `is_running`

Category: `sense` -- Shape: `BOOLEAN`

Parameters: none

### `is_crouching`

Category: `sense` -- Shape: `BOOLEAN`

Parameters: none

### `is_in_tag`

Category: `sense` -- Shape: `BOOLEAN`

Parameters:
- `target` (`TEXT`), default ``
- `tag` (`TEXT`), default ``

### `set_variable`

Category: `variables` -- Shape: `COMMAND`

Parameters:
- `name` (`TEXT`), default ``
- `value` (`NUMBER`), default `0`

### `get_variable`

Category: `variables` -- Shape: `REPORTER`

Parameters:
- `name` (`TEXT`), default ``

### `set_variable_permanent`

Category: `variables` -- Shape: `COMMAND`

Parameters:
- `name` (`TEXT`), default ``

### `new_list`

Category: `variables` -- Shape: `COMMAND`

Parameters:
- `name` (`TEXT`), default ``

### `get_list`

Category: `variables` -- Shape: `REPORTER`

Parameters:
- `name` (`TEXT`), default ``

### `add_list`

Category: `variables` -- Shape: `COMMAND`

Parameters:
- `name` (`TEXT`), default ``
- `list` (`LIST`), default ``

### `add_value`

Category: `variables` -- Shape: `COMMAND`

Parameters:
- `name` (`TEXT`), default ``
- `value` (`TEXT`), default ``

### `set_list_kv`

Category: `variables` -- Shape: `COMMAND`

Parameters:
- `name` (`TEXT`), default ``
- `key` (`NUMBER`), default ``
- `value` (`TEXT`), default ``

### `set_list_list`

Category: `variables` -- Shape: `COMMAND`

Parameters:
- `name` (`TEXT`), default ``
- `list` (`LIST`), default ``

### `get_list_value`

Category: `variables` -- Shape: `REPORTER`

Parameters:
- `name` (`TEXT`), default ``
- `key` (`NUMBER`), default ``

### `set_list_permanent`

Category: `variables` -- Shape: `COMMAND`

Parameters:
- `name` (`TEXT`), default ``

### `clear_list`

Category: `variables` -- Shape: `COMMAND`

Parameters:
- `name` (`TEXT`), default ``

