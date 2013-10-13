var app = angular.module('sigtrac', ['ngResource']);

app.config(function($interpolateProvider) {
  $interpolateProvider.startSymbol("[[");
  $interpolateProvider.endSymbol("]]");
});

app.factory('averages', [
    '$resource', function($resource){
        return $resource('/results');
}])

app.controller('MainCtrl', function($scope, averages) {
  $scope.model = { name: 'World' };
  $scope.averages = averages.query();
});



