# Custom themes

Built-in themes are selected from the _Theme_ setting. On top of those, you can
define your own theme by opening _Settings > Style_, scrolling to the themes
list and tapping _Add a custom theme_.

A custom theme starts from one of the built-in themes, called its **base
theme**, and then overrides individual colours. Anything you do not override
keeps the colour of the base theme, so you only need to change what you care
about.

Editing is done with colour pickers, and a preview is drawn while you edit so
that you can see the result immediately. Tap a colour swatch to change it,
long-press it to remove the override and go back to the base theme colour.

Saving a theme makes it the active one. Tap a saved theme to change it, or use
the _Remove theme_ button to delete it. Removing a theme goes back to the
system theme.

## Colours that can be changed

The names below are the same as the attribute names used in
[`res/values/themes.xml`](https://github.com/Julow/Unexpected-Keyboard/blob/master/res/values/themes.xml),
so the built-in themes and custom themes read the same way.

| Name | What it colours |
| --- | --- |
| `colorKeyboard` | The background behind the keys |
| `colorKey` | The keys |
| `colorKeyActivated` | A key while it is pressed |
| `colorKeyAction` | The action keys, like Shift and Enter |
| `colorKeySpaceBar` | The space bar |
| `colorLabel` | The letters printed on the keys |
| `colorLabelActivated` | A letter while its modifier is active |
| `colorLabelPressed` | A letter while its key is pressed |
| `colorLabelLocked` | A letter while Shift is locked |
| `colorSubLabel` | The smaller labels in the corners of keys |
| `emoji_button_bg` | The background of the emoji category buttons |
| `emoji_color` | The emojis in the grid |
| `emoji_key_bg` | The background behind the emoji grid |
| `emoji_key_text` | The category buttons of the emoji pane |
| `clipboard_divider_color` | The lines between clipboard entries |

## How a custom theme is stored

Custom themes are stored as JSON in the app preferences. The format is stable
enough to be edited by hand or backed up:

```json
{
  "name": "Mine",
  "base": "dark",
  "colors": {
    "colorKey": "#292e42",
    "colorLabel": "#c0caf5"
  },
  "keyBorderRadiusDp": 6
}
```

* `name` is the name shown in the themes list, and also how the theme is
  referred to in the _Theme_ setting, as `custom:<name>`.
* `base` is the name of a built-in theme, one of the values in
  `res/values/arrays.xml`, for instance `dark`, `light`, `tokyonight` or
  `system`.
* `colors` maps a colour name to a hexadecimal value. `#rgb`, `#argb`,
  `#rrggbb` and `#aarrggbb` are all accepted. Unknown names and malformed
  values are ignored, so a theme saved by a newer version of the app still
  loads.
* `keyBorderRadiusDp` is the corner radius of the keys in density independent
  pixels. It is optional.

We recommend keeping a copy of your themes outside of Unexpected Keyboard. If
you install a build signed with a different certificate, the app cannot read
the preferences of the previously installed one, and your themes would be lost.

## Related

* [Custom layouts](Custom-layouts.md) for changing the keys themselves.