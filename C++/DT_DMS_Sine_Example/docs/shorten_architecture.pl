#!/usr/bin/perl
# Makes a one-page version of the architecture graph of the C++ sine example. The input is the Doxygen
# collaboration graph cleaned by merge_template_nodes.pl. The script:
#   - keeps, for each class listed below, only the fields and methods that the sine example uses
#     (classes that are not listed are kept whole);
#   - writes each member compactly, as in a UML overview: "name" for a field, "name()" for a method;
#   - removes the side classes listed in @drop;
#   - tightens the spacing of the graph and draws it in black and white.
#
# Usage:  perl shorten_architecture.pl DT_DMS_Sine_Architecture.dot > DT_DMS_Sine_Architecture_short.dot
use strict;
use warnings;

my %keep = (
    'DT_DMS'       => [qw(name dataList modelList serviceList mu delta clock
                          DT_DMS tNow setClock addData addModel addService runService
                          checkConsistency dumpGraphviz)],
    'DT_Data'      => [qw(source dataType observers currentData currentTime collectedData clock characterization
                          DT_Data registerObserver notifyObservers updateData getCurrentTime
                          getRealTimeData getHistoricalData setClock getCharacterization)],
    'DT_DataCharacterization' => [qw(unit signalType dataFlowType setUnit setSignalType setDataFlowType)],
    'DT_Automaton' => [qw(parameters inputs outputs state phases transitions currentPhase elapsed
                          addParameter addInput addOutput addStateVariable addPhase addTransition
                          setInitialization initialize externalTransition internalTransition
                          getCurrentPhase getState)],
    'DT_Model'     => [qw(modelName servicesToBeUpdated DT_Model assimilate executeModel evaluate
                          addServiceToBeUpdated notifyServices)],
    'DT_Model_Descriptive' => [qw(dataSource dataPointCount dataSummary minimum maximum sum
                                  DT_Model_Descriptive update executeModel)],
    'DT_Model_Descr_CubicBSpline' => [qw(step leftEndpoint controlPoints DT_Model_Descr_CubicBSpline
                                         update getSineModelValue evaluate getLastTime getStep specifyAutomaton)],
    'DT_Service'   => [qw(serviceName serviceDescription model consumedData
                          DT_Service update updateWithModel execute executeService setModel)],
    'DT_Service_Diagnosis' => [qw(normalMinimum normalMaximum diagnosisReport
                                  DT_Service_Diagnosis executeService)],
    'DT_Service_Diagnosis_Sine' => [qw(threshold anomalyTimes DT_Service_Diagnosis_Sine executeService
                                       residualOf performRandomCheck getAnomalyTimes specifyAutomaton)],
    'DT_Mu'        => [qw(delta clock models sources pending
                          DT_Mu addModel observe mu assimilate assimilateSynchronously
                          assimilateAsynchronously flush)],
);
my @drop = ('DT_Automaton::Initialization', 'std::enable_shared_from_this<DT_Mu>');

my %keep_set = map { my $c = $_; $c => { map { $_ => 1 } @{ $keep{$c} } } } keys %keep;
my %drop_set = map { $_ => 1 } @drop;

sub plain {                                   # text of an HTML cell without tags and entities
    my $t = shift;
    $t =~ s/<[^>]*>//g;
    $t =~ s/&lt;/</g; $t =~ s/&gt;/>/g; $t =~ s/&amp;/&/g;
    return $t;
}

sub class_of {                                # class name of a node statement, without namespace and <T>
    my ($title) = $_[0] =~ /<TD COLSPAN="2"[^>]*>(.*?)<\/TD><\/TR>/s or return '';
    my $c = plain($title);
    $c =~ s/\s+//g;
    $c =~ s/<T>//g;
    $c =~ s/^(Automaton|Data|Models|Mu|Services|Visualization):://;
    return $c;
}

local $/;
my $dot = <>;
my ($head, $body) = $dot =~ /^(.*?\n)(  Node\d+ .*)$/s or die "Not a Doxygen graph\n";
$head =~ s/(\n  node \[)/\n  ranksep=0.3;\n  nodesep=0.15;$1/;
my ($tail) = $body =~ /(\n\}\s*)$/;
$body =~ s/\n\}\s*$//;
my @statements = split /(?<=\];)\n/, $body;   # every statement ends with "];"

# 1. the nodes to remove
my %removed;
for (@statements) {
    $removed{$1} = 1 if /^\s*(Node\d+) \[/ && $drop_set{ class_of($_) };
}

# 2. rewrite the remaining statements
my @out;
for my $st (@statements) {
    if ($st =~ /^\s*(Node\d+) -> (Node\d+) \[/) {                     # an edge
        next if $removed{$1} || $removed{$2};
        push @out, $st;
        next;
    }
    my ($id) = $st =~ /^\s*(Node\d+) \[/;
    next if defined $id && $removed{$id};
    my $class = class_of($st);
    my (@lines, %seen);
    for my $line (split /\n/, $st) {
        if ($line =~ /^(<TR><TD VALIGN="top" CELLPADDING="1" CELLSPACING="0">[^<]*<\/TD><TD [^>]*>)(.*)(<\/TD><\/TR>.*)$/) {
            my ($before, $text, $after) = ($1, plain($2), $3);
            my $is_method = $text =~ /\(/;
            my ($name) = $is_method ? $text =~ /(\w+)\s*\(/ : $text =~ /(\w+)\s*$/;
            next unless defined $name;
            next if $keep_set{$class} && !$keep_set{$class}{$name};
            my $short = $is_method ? "$name()" : $name;
            next if $seen{$short}++;                                  # overloaded methods appear once
            $line = "$before$short<BR ALIGN=\"LEFT\"/>$after";
        }
        push @lines, $line;
    }
    push @out, join("\n", @lines);
}

# 3. black and white, for print
my $result = $head . join("\n", @out) . $tail;
$result =~ s/color="steelblue1"/color="black"/g;
$result =~ s/fontcolor="grey"/fontcolor="black"/g;
$result =~ s/fillcolor="grey60"/fillcolor="grey88"/g;
print $result;
