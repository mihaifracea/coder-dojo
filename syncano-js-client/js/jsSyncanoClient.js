/*var syncano = SyncanoConnector.getInstance();
syncano.connect( {instance: 'ancient-lake-594787', api_key: '48a970458fd79070f3fff6f967987cd60ae36834'}, function(auth) {
  console.log("Connected")
  syncano.Data.new( 6463, 'Default', {title: "My title"}, function() {
  	console.log("Saved!")
  });
});*/


var syncano = SyncanoConnector.getInstance();
var projectId = 6465;
var collectionId = 19033;

var params = {
    title: 'This is totally a title',
    text: 'Hi!  Welcome to Syncano.',
    state: 'Moderated',
    folder: 'img',
    additional: {
      start_date: 'yesterday',
      end_date: 'tomorrow'
    }
}

syncano.connect({
        instance: 'ancient-lake-594787',
        api_key: '48a970458fd79070f3fff6f967987cd60ae36834'
    }, function(auth){
    	console.log('connected');
        syncano.Data.new(projectId, collectionId, params, function (data) {
                console.log('Created new data object with ID = ', data.id);
            });

        var paramsRead = {
    		include_children: true,
    		folders: 'Default'
  		};
  
  		syncano.Data.get(projectId, collectionId, paramsRead, function (data) {
    		console.log('Received', data.length, 'objects');
    		data.forEach(function (d) {
      			console.log(d);
    		});
  		});
});
