$(function () {
  $(`.logout-button`).on('click', function () {
    window.location.replace("logout/");
  })

  $('.upload-button').on('click', function (e) {
    e.preventDefault();

    let data = new FormData();
    data.append("uploadFile", document.getElementById("file-input").files[0]);

    $.ajax({
      url: 'uploadFile',
      type: 'POST',
      data: data,
      cache: false,
      processData: false,
      success: function(response){
          window.location = `/uploadFile?fileName=${response}`
      },
      error: function(error){
        console.log('ОШИБКИ AJAX запроса: ' + error);
      }
    });
  })
});