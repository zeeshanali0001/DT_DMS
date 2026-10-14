#!/bin/sh
# Renders a hand-positioned diagram (PDF, SVG and PNG) with Graphviz.
#
# Usage (from the docs folder, in Git Bash):   sh make_overview.sh DT_DMS_Sine_Architecture_Simplified
#
# The .dot file fixes the position of every node (in inches). Graphviz routes the lines, except the
# lines that the .dot file redraws by hand with comments of the form
#     // anchor NODE x,y                 a node and its position, used to convert inches to points
#     // curve ID  sx,sy | x,y x,y ...   the curve of the edge [id=ID]: arrow tip, then Bezier points
name="$1"
neato -Tdot "$name.dot" > "$name.laid.dot" || exit 1
perl -e '
    use strict; use warnings;
    my ($src, $laid) = @ARGV;
    local $/;
    open my $s, "<", $src or die; my $source = <$s>; close $s;
    open my $l, "<", $laid or die; my $dot = <$l>; close $l;
    my ($node, $ax, $ay) = $source =~ m{// anchor (\w+) ([\d.]+),([\d.]+)} or die "no anchor\n";
    my ($px, $py) = $dot =~ /\n\t\Q$node\E\t\[.*?pos="([\d.]+),([\d.]+)"/s or die "anchor node not found\n";
    my $pt = sub { my ($x, $y) = split /,/, shift;
                   sprintf "%.2f,%.2f", $px + ($x - $ax) * 72, $py + ($y - $ay) * 72 };
    while ($source =~ m{// curve (\w+)\s+([\d.,]+) \| ([\d., ]+)}g) {
        my ($id, $tip, $points) = ($1, $2, $3);
        my $pos = "s," . $pt->($tip) . " " . join(" ", map { $pt->($_) } split " ", $points);
        $dot =~ s/(id=\Q$id\E,\s*pos=")[^"]*(")/$1$pos$2/ or die "edge $id not found\n";
    }
    print $dot;
' "$name.dot" "$name.laid.dot" > "$name.final.dot" || exit 1
neato -n2 -Tpdf "$name.final.dot" -o "$name.pdf"
neato -n2 -Tsvg "$name.final.dot" -o "$name.svg"
neato -n2 -Tpng -Gdpi=300 "$name.final.dot" -o "$name.png"
rm -f "$name.laid.dot" "$name.final.dot"
echo "Rendered $name.pdf, $name.svg and $name.png"
