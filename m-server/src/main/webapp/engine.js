$(function () {

    Highcharts.setOptions({global: {useUTC: false}});

    /**
     * Main data part. We take it from server
     */
    var charts = void 0;

    function convertDataToPoint(dataPoint) {
        return [dataPoint.timestamp, dataPoint.value != void 0 ? dataPoint.value : null];
    }

    function convertDataToSeries(data) {
        var series = [];
        for (var metric in data) {
            if (!metric) continue;

            var seriesData = [];

            data[metric].forEach(function (tAndV) {
                seriesData.push(convertDataToPoint(tAndV));
            });

            series.push({
                name: metric,
                data: seriesData,
                dataGrouping: {
                    enabled: false
                }
            });
        }
        return series;
    }

    function reload(chart, min, max) {
        var highchart = chart.highchart;
        var minParameter = min != void 0 ? '&min=' + min : '';
        var maxParameter = max != void 0 ? '&max=' + max : '';

        $.getJSON('data?metric=' + encodeURIComponent(chart.metric) + minParameter + maxParameter + '&callback=?', function (data) {
            if (data === 'restoring-failed' || data === 'restoring-in-progress') {

            } else {
                for (var metric in data) {
                    if (!metric) continue;

                    var series = void 0;
                    for (var s = 0; s < highchart.series.length; s++) {
                        if (highchart.series[s].name === metric) series = highchart.series[s];
                    }

                    var seriesData = [];
                    data[metric].forEach(function (tAndV) {
                        seriesData.push(convertDataToPoint(tAndV));
                    });

                    series.setData(seriesData);
                }
            }
        });
    }

    /**
     * Load new data depending on the selected min and max
     */
    function afterSetExtremes(e) {
        var hc = this.chart;
        charts.forEach(function (chart) {
            if (chart.highchart === hc) {
                reload(chart, Math.round(e.min), Math.round(e.max));
            }
        });
    }

    /**
     * In order to synchronize tooltips and crosshairs, override the
     * built-in events with handlers defined on the parent element.
     */
    $('#container').bind('mousemove touchmove touchstart', function (e) {
        var chart, point, event;

        for (var i = 0; i < Highcharts.charts.length; i = i + 1) {
            chart = Highcharts.charts[i];
            event = chart.pointer.normalize(e.originalEvent); // Find coordinates within the chart
            var series = chart.series[0];
            if (!series) continue; // no data in that chart
            point = series.searchPoint(event, true); // Get the hovered point

            // console.log(point);
            if (point) {
                point.onMouseOver(); // Show the hover marker
                // chart.tooltip.refresh(point); // Show the tooltip
                chart.xAxis[0].drawCrosshair(event, point); // Show the crosshair
            }
        }
    });

    /**
     * Override the reset function, we don't need to hide the tooltips and crosshairs.
     */
    Highcharts.Pointer.prototype.reset = function () {
        return undefined;
    };

    /**
     * Synchronize zooming through the setExtremes event handler.
     */
    function syncExtremes(e) {
        var thisChart = this.chart;

        if (e.trigger !== 'syncExtremes') { // Prevent feedback loop
            Highcharts.each(Highcharts.charts, function (chart) {
                if (chart !== thisChart) {
                    if (chart.xAxis[0].setExtremes) { // It is null while updating
                        chart.xAxis[0].setExtremes(e.min, e.max, undefined, false, {trigger: 'syncExtremes'});
                    }
                }
            });
        }
    }

    function load(chart) {
        var chartDiv = $('<div style="height: 400px; min-width: 310px"></div>').appendTo('#container');
        loadToChart(chart, chartDiv);
    }

    function loadToChart(chart, chartDiv) {
        $.getJSON('data?metric=' + encodeURIComponent(chart.metric) + '&callback=?', function (data) {
            var series = convertDataToSeries(data);

            var valueFormatter = void 0;
            if (chart.type === 'gb') {
                valueFormatter = function (v) {
                    var kb = 1024;
                    var mb = 1024 * kb;
                    var gb = 1024 * mb;
                    if (v > gb) return Math.round(v / gb) + 'gb';
                    else if (v > mb) return Math.round(v / mb) + 'mb';
                    else if (v > kb) return Math.round(v / kb) + 'kb';
                    return v + 'b';
                }
            } else if (chart.type === 'percent') {
                valueFormatter = function (v) {
                    return v + '%';
                }
            }

            chartDiv.highcharts('StockChart', {
                chart: {zoomType: 'x'},
                title: {text: chart.title},
                navigator: {adaptToUpdatedData: false, series: series},
                scrollbar: {enabled: false},
                tooltip: {
                    pointFormatter: valueFormatter ? function () {
                        return '<span style="color:{point.color}">\u25CF</span> ' + this.series.name + ': <b>' + valueFormatter(this.y) + '</b><br/>';
                    } : void 0
                },
                rangeSelector: {
                    buttons: [
                        {type: 'minute', count: 15, text: '15m'},
                        {type: 'minute', count: 30, text: '30m'},
                        {type: 'hour', count: 1, text: '1h'},
                        {type: 'hour', count: 2, text: '2h'},
                        {type: 'hour', count: 3, text: '3h'},
                        {type: 'day', count: 1, text: '1d'},
                        {type: 'all', text: 'All'}],
                    inputEnabled: false, // it supports only days
                    selected: 4 // all
                },
                xAxis: {
                    events: {
                        setExtremes: syncExtremes,
                        afterSetExtremes: afterSetExtremes
                    },
                    minRange: 60 * 1000 // one hour
                },
                yAxis: {
                    floor: 0,
                    labels: {
                        formatter: valueFormatter ? function () {
                            return valueFormatter(this.value);
                        } : void 0
                    }
                },
                series: series
            });
            chart.highchart = Highcharts.charts[Highcharts.charts.length - 1];
        });
    }

    function checkStatus() {
        $.getJSON('status', function (status) {
            var message = '';

            if (status.events.error === 'restoring-failed') {
                message = 'Failed on event restoring!';
            } else if (status.events.error === 'restoring-in-progress') {
                message = 'Restoring events...';
            } else {
                message = 'Events: ' + status.events.count + ' on disk ' + Math.round(status.events.space / 1024 / 1024) + 'Mb';
            }
            $('#events-status').text(message);

            var total = 0;
            var failed = 0;
            var live = 0;
            var starting = 0;

            for (var host in status.nodes) {
                total++;
                if (status.nodes[host].status === 'STARTING') starting++;
                if (status.nodes[host].status === 'FAILED') failed++;
                if (status.nodes[host].status === 'LIVE') live++;
            }
            $('#nodes-status').text('Nodes: ' + live
                + (starting > 0 ? ' starting ' + starting + '...' : '')
                + (failed > 0 ? ' failed ' + failed + '!' : '') + '/' + total);
        });
    }

    function startSpaceChecker() {
        window.setInterval(checkStatus, 5000);
        checkStatus();
    }

    $(document).ready(function () {
        $.getJSON('config', function (config) {
            charts = config;
            charts.forEach(load);
            startSpaceChecker();

            $('#clear-events').click(function () {
                if (confirm('Do you want to remove all metric data?')) {
                    $.post('data/clear', {}, function () {
                        window.location.reload(true);
                    });
                }
            });

            // $('#restart-nodes').click(function () {
            //     $.post('nodes/restart', {}, function () {
            //     });
            // });
        });
    });

});
