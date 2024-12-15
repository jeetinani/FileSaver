import { useState } from "react";
import { useParams } from "react-router-dom";
import axios, { HttpStatusCode } from "axios";

export default function Retreive({ uuid }) {

    const [alert, setAlert] = useState("");

    const downloadHandler = async (e) => {
        e.preventDefault();

        try {
            const response = await axios.get(`/retrieve/${uuid}?passcode=`+e.target.passcode.value, {
                responseType: "blob",
            });

            const contentDisposition = response.headers["content-disposition"];
            const fileName = contentDisposition
                ? contentDisposition.split("filename=")[1]?.replace(/"/g, "")
                : "downloaded_file";

            const blob = new Blob([response.data], { type: response.headers["content-type"] });

            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", fileName); 
            document.body.appendChild(link);
            link.click();

            link.remove();
            window.URL.revokeObjectURL(url);


            /* axios.get("/retrieve/"+uuid+"?passcode="+e.target.passcode.value)
    .then(resp=>{
                setAlert("File Downloaded");
            }).catch((error) => {
                // Handle errors, if any
                //console.log(JSON.stringify(error));
                console.error("Login failed:", error);
                if (error.response.status === HttpStatusCode.BadRequest) {
                    setAlert("Invalid Passcode");
                }else if (error.response.status === HttpStatusCode.NotFound) {
                    setAlert("File not found");
                }else {
                    setAlert("download failed");
                }
            } */
        } catch (error) {
            //console.log(JSON.stringify(error));
            console.error("Login failed:", error);
            if (error.response.status === HttpStatusCode.BadRequest) {
                setAlert("Invalid Passcode");
            } else if (error.response.status === HttpStatusCode.NotFound) {
                setAlert("File not found");
            } else {
                setAlert("download failed");
            }
        }
    };

    return (
        <>
            <div>
                <div className="row">
                    <div className="col:6 mx-auto">
                        <h3>Download</h3>
                        <form onSubmit={downloadHandler}>

                            <div className="mb-3">
                                <label htmlFor="passcode" className="form-label">Passcode</label>
                                <input type="text" className="form-control" id="passcode"
                                    name="passcode" required placeholder="Passcode" />
                            </div>
                            <div className="mb-3">
                                <button type="submit" className="btn btn-success" name="upload">Download</button>
                            </div>
                            <div>
                                {alert}
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </>
    )
}