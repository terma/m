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
        var chartDiv = $('<div style="height: 400px; min-width: 310px"></div>').appendTo('#container');
        loadToChart(chart, chartDiv);
    }

    function loadToChart(chart, chartDiv) {
        $.getJSON('data?metric=' + encodeURIComponent(chart.metric) + '&callback=?', function (data) {
            var series = convertDataToSeries(data);
            chartDiv.highcharts('StockChart', {
                chart: {zoomType: 'x'},
                title: {text: chart.title},
                navigator: {adaptToUpdatedData: false, series: series},
                scrollbar: {enabled: false},
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
                yAxis: {floor: 0},
                series: series
            });
            chart.highchart = Highcharts.charts[Highcharts.charts.length - 1];
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
        });
    });

});
