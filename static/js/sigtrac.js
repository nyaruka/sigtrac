var app = angular.module('sigtrac', ['ngResource', 'highcharts-ng']);

app.config(function($interpolateProvider) {
  $interpolateProvider.startSymbol("[[");
  $interpolateProvider.endSymbol("]]");
});

app.factory('averages', [
    '$resource', function($resource){
        return $resource('/results');
}])

app.factory('carriers', [
    '$resource', function($resource){
        return $resource('/carriers');
}])

app.factory('series', function($http) {
  return $http.get('/series');
});

app.controller('MainCtrl', function($scope, averages, carriers, series) {
  $scope.model = { name: 'World' };
  $scope.averages = averages.query();
  $scope.series = {};

  $scope.setDelta = function(delta) {
    $scope.currentDelta = delta;
    $scope.averages[0]['current'] = $scope.averages[0][delta];
    $scope.currentSeries = $scope.series[delta];
  }

  // later: from an API call?
  var intervals = ['hour', 'day', 'week']

  series.then(function(seriesData) {
    carriers.query().$then(function(carrierData){
      angular.forEach(intervals, function(interval){
        tempSeries = []
        angular.forEach(carrierData.data, function(carrier) {
          carrierSeries = {
            name: carrier.slug,
            color: carrier.color,
            data: formatPoints(seriesData.data[interval][carrier.slug])
          };
          tempSeries.push(carrierSeries);
        });
        $scope.series[interval] = tempSeries;
      });
      $scope.setDelta('day');
    });
  });

  var formatPoints = function(rawPoints) {
    angular.forEach(rawPoints, function(point) {
      point[0] = Date.parse(point[0]);
    });
    return rawPoints;
  };

  $scope.$watch('currentSeries', function(newValue, oldValue) { 
    $scope.chartConfig['series'] = $scope.currentSeries; 
  });

  $scope.chartConfig = {
    options: {
      chart: {
        backgroundColor: null,
        type: 'column',
        height: 200,
	marginBottom: 0
      },
      plotOptions: {
        column: {
          pointPadding: 0,
          borderWidth: 0,
        }
      },
      legend: {
        enabled: false,
      },
      xAxis: {
        type: 'datetime',
        labels: {
          enabled: false,
        }
      },
      yAxis: {
        title: {
          text: null,
        },
        labels: {
          enabled: false,
        },
        gridLineWidth: 0,
      },
      tooltip: {
        useHTML:true,
        formatter: function() {
          return '<div class="chart-tooltip carrier ' + this.series.name + '">'+
                 '<div class=label-speed>' + Math.floor(this.y).toLocaleString() +
                 '</div><div class=units>kbps</div>' + '<div class=time>' +
                 Highcharts.dateFormat('%b %d, %H:%M', this.x) + '</div></div>';
        }
      }
    },
    series: $scope.currentSeries,
    title: {
      text: null,
    },
  }
});
