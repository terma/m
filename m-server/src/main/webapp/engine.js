$(function () {

    /**
     * Main data part. We take it from server
     */
    var charts = void 0;

    function convertDataToSeries(data) {
        var series = [];
        for (var metric in data) {
            if (!metric) continue;

            var seriesData = [];

            data[metric].forEach(function (tAndV) {
                seriesData.push([tAndV.timestamp, tAndV.value]);
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
            for (var metric in data) {
                if (!metric) continue;

                var series = void 0;
                for (var s = 0; s < highchart.series.length; s++) {
                    if (highchart.series[s].name === metric) series = highchart.series[s];
                }

                var seriesData = [];
                data[metric].forEach(function (tAndV) {
                    seriesData.push([tAndV.timestamp, tAndV.value]);
                });

                series.setData(seriesData);

                // var dataMax = data[metric][data[metric].length - 1].timestamp;
                // var chartMax = chart.highchart.xAxis[0].max;
                // console.log('data max ' + dataMax + ', chart max ' + chartMax + ', d vs c ' + (dataMax - chartMax));
            }
        });
    }

    /**
     * Load new data depending on the selected min and max
     */
    function afterSetExtremes(e) {
        charts.forEach(function (chart) {
            reload(chart, Math.round(e.min), Math.round(e.max));
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
        $.getJSON('data?metric=' + encodeURIComponent(chart.metric) + '&callback=?', function (data) {
            var series = convertDataToSeries(data);

            $('<div style="height: 400px; min-width: 310px"></div>').appendTo('#container').highcharts('StockChart', {
                chart: {zoomType: 'x'},
                title: {text: chart.title},
                navigator: {adaptToUpdatedData: false, series: series},
                scrollbar: {enabled: false},
                rangeSelector: {
                    buttons: [
                        {type: 'minute', count: 30, text: '30m'},
                        {type: 'hour', count: 1, text: '1h'},
                        {type: 'hour', count: 2, text: '2h'},
                        {type: 'day', count: 1, text: '1d'},
                        {type: 'week', count: 1, text: '1w'},
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
                yAxis: {floor: 0},
                series: series
            });
            chart.highchart = Highcharts.charts[Highcharts.charts.length - 1];
            // console.log(chart.highchart.xAxis[0].max);
        });
    }

    $('#clear').click(function () {
        if (confirm('Do you want to remove all metric data?')) {
            $.post('data/clear', {}, function () {
                charts.forEach(function (chart) {
                    reload(chart)
                });
            });
        }
    });

    $(document).ready(function () {
        $.getJSON('config', function (config) {
            charts = config;
            charts.forEach(load);
            window.setInterval(function () {
                // function reloadNavigator(chart) {
                //         $.getJSON('data?metric=' + chart.metric + '&callback=?', function (data) {
                //             chart.highchart.navigator.series = convertDataToSeries(data);
                //         });
                //     }
                //
                // charts.forEach(function (chart) {
                //     reload(chart, chart.highchart.xAxis[0].min);
                // });
            }, 5000);
        });
    });

});
