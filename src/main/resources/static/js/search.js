function querySubmit(){

        var codequery = $("#codequery").val();
        window.alert(codequery);

        if(codequery.length == 0){
            $("#codequery").tips({msg:"please input query code"});
            return;
        }

        $.ajax({
            url: "./issueSearch",
            type: "post",
            data: {"codequery":codequery},
            dataType: "json",
            success: function(data){
                if(data != null){

                }
                else{
                }
            },
        });
    }
